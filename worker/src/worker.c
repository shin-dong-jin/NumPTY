#define _GNU_SOURCE
#include "worker.h"

#include "base/config.h"
#include "base/protocol.h"
#include "base/signal_handler.h"
#include "calculator/calculator.h"
#include "calculator/gmp_random.h"
#include "calculator/number_theory/eratosthenes_sieve.h"
#include "calculator/number_theory/exec_number_theory.h"
#include "common.h"
#include "logger.h"
#include "redis/redis_client.h"
#include "transport/egress.h"
#include "transport/ingress.h"

static const task_exec_fn exec_table[] = {
    [GCD] = exec_gcd,
    [LCM] = exec_lcm,
    [EXTENDED_GCD] = exec_extended_euclidean,
    [PRIMALITY_TEST] = exec_primality_test_miller_rabin,
    [FACTORIZE] = exec_factorize};

static calculate_status_e exec_task(base_task_t* task) {
    if (task == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to exec_task.");
        return CALCULATE_ERR_ARGS;
    }

    if (task->task_type >= UNKNOWN_TASK || task->task_type < 0) {
        LOG_ERROR("Fatal error: task->task_type is UNKNOWN or not valid.");
        return CALCULATE_ERR_ARGS;
    }

    strncpy(task->algorithm, get_task_algorithm_name(task->task_type),
            sizeof(task->algorithm) - 1);
    task->algorithm[sizeof(task->algorithm) - 1] = '\0';

    task_exec_fn fn = exec_table[task->task_type];
    if (fn == NULL) {
        LOG_ERROR("Fatal error: task_exec_fn is NULL.");
        return CALCULATE_ERR_ARGS;
    }

    struct timespec start, end;
    clock_gettime(CLOCK_MONOTONIC, &start);

    calculate_status_e calculate_status = fn(task);

    clock_gettime(CLOCK_MONOTONIC, &end);
    task->elapsed_time_ms = (end.tv_sec - start.tv_sec) * 1000.0 +
                            (end.tv_nsec - start.tv_nsec) / 1000000.0;

    return calculate_status;
}

worker_status_e run(worker_config_t* worker_config,
                    redis_client_t* redis_client) {
    if (worker_config == NULL || redis_client == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer is passed to run.");
        return WORKER_FAILED;
    }

    if (initialize_rstate() != CALCULATE_SUCCESS) {
        LOG_ERROR("Failed to initialize rstate");
        return WORKER_FAILED;
    }

    if (initialize_sieve() != CALCULATE_SUCCESS) {
        LOG_ERROR("Failed to initialize sieve");
        return WORKER_FAILED;
    }

    LOG_INFO("Waiting for request stream...");

    while (keep_running) {
        base_task_t* task = NULL;

        ingress_status_e fetch_status =
            ingress_fetch_task(worker_config, redis_client, &task);

        if (fetch_status == INGRESS_TIMEOUT) {
            continue;
        }

        if (fetch_status == INGRESS_SHUTDOWN) {
            break;
        }

        if (fetch_status < 0) {
            if ((fetch_status == INGRESS_ERR_PARSE ||
                 fetch_status == INGRESS_ERR_FORMAT) &&
                task != NULL && task->task_id[0] != '\0') {
                LOG_INFO("Sending FAILED status for corrupted payload. ID: %s",
                         task->task_id);
                task->status = FAILED;

                if (egress_send_status(worker_config, redis_client, task) !=
                    EGRESS_SUCCESS) {
                    LOG_ERROR(
                        "Error occurred while sending failed status on Redis "
                        "Stream. ID: %s",
                        task->task_id);
                }

                free_task(&task);
            } else if (fetch_status == INGRESS_ERR_CONN) {
                sleep(1);
            } else {
                LOG_ERROR("Fatal ingress fetch error: %d", fetch_status);
                break;
            }

            continue;
        }

        LOG_DEBUG("Starting task. Request ID: %s", task->task_id);

        task->status = WORKING;
        if (egress_send_status(worker_config, redis_client, task) !=
            EGRESS_SUCCESS) {
            LOG_ERROR(
                "Error occurred while sending WORKING status on Redis Stream. "
                "Skipping calculation "
                "for ID: %s",
                task->task_id);
            free_task(&task);
            continue;
        }

        if (exec_task(task) == CALCULATE_SUCCESS) {
            LOG_DEBUG("Task COMPLETED. ID: %s", task->task_id);
            task->status = COMPLETED;
        } else {
            LOG_ERROR("Task FAILED. Task type: %s, ID: %s",
                      get_task_type_name(task->task_type), task->task_id);
            task->status = FAILED;
        }

        egress_status_e egress_status =
            egress_send_result(worker_config, redis_client, task);

        if (egress_status != EGRESS_SUCCESS) {
            if (egress_status == EGRESS_ERR_CONN) {
                LOG_ERROR(
                    "Fatal Connection Error!! FAILED to send result for ID %s",
                    task->task_id);
            } else {
                LOG_ERROR("Internal egress error(%d) for ID %s", egress_status,
                          task->task_id);
            }
        } else {
            if (egress_send_xack(worker_config, redis_client, task) !=
                EGRESS_SUCCESS) {
                LOG_ERROR(
                    "Fatal Connection Error!! Failed to ACK Task ID %s. It "
                    "will remain in PEL for "
                    "retry.",
                    task->task_id);
            }
        }

        free_task(&task);
    }

    cleanup_rstate();
    cleanup_sieve();

    fprintf(stdout, "\n");
    LOG_INFO("Break out of the worker loop.");
    return WORKER_SUCCESS;
}
