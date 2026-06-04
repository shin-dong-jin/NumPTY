#include "base/protocol.h"

#include "common.h"
#include "logger.h"

const char* get_status_name(task_status_e task_status) {
    switch (task_status) {
        case PENDING:
            return "PENDING";
        case WORKING:
            return "WORKING";
        case COMPLETED:
            return "COMPLETED";
        case FAILED:
            return "FAILED";
        case UNKNOWN:
            return "UNKNOWN";
    }

    return "UNHANDLED";
}

const char* get_task_type_name(task_type_e task_type) {
    switch (task_type) {
        case GCD:
            return "GCD";
        case LCM:
            return "LCM";
        case EXTENDED_GCD:
            return "EXTENDED_GCD";
        case PRIMALITY_TEST:
            return "PRIMALITY_TEST";
        case FACTORIZE:
            return "FACTORIZE";
        case UNKNOWN_TASK:
            return "UNKNOWN_TASK";
    }

    return "UNHANDLED_TASK";
}

const char* get_task_algorithm_name(task_type_e task_type) {
    switch (task_type) {
        case GCD:
            return "EUCLIDEAN";
        case LCM:
            return "EUCLIDEAN";
        case EXTENDED_GCD:
            return "EXTENDED_EUCLIDEAN";
        case PRIMALITY_TEST:
            return "MILLER_RABIN";
        case FACTORIZE:
            return "TRIAL,POLLARD_RHO,ECM";
        case UNKNOWN_TASK:
            return "UNKNOWN_ALGORITHM";
    }

    return "UNHANDLED_ALGORITHM";
}

base_task_t* create_task(task_type_e task_type) {
    base_task_t* task = NULL;

    switch (task_type) {
        case GCD:
            task = (base_task_t*)malloc(sizeof(gcd_task_t));
            if (task == NULL) {
                LOG_ERROR(
                    "Fatal Error: failed to allocate memory for gcd task.");
                return NULL;
            }
            memset(task, 0, sizeof(gcd_task_t));
            break;
        case LCM:
            task = (base_task_t*)malloc(sizeof(lcm_task_t));
            if (task == NULL) {
                LOG_ERROR(
                    "Fatal Error: failed to allocate memory for lcm task.");
                return NULL;
            }
            memset(task, 0, sizeof(lcm_task_t));
            break;
        case EXTENDED_GCD:
            task = (base_task_t*)malloc(sizeof(extended_euclidean_task_t));
            if (task == NULL) {
                LOG_ERROR(
                    "Fatal Error: failed to allocate memory for extended "
                    "euclidean task.");
                return NULL;
            }
            memset(task, 0, sizeof(extended_euclidean_task_t));
            break;
        case PRIMALITY_TEST:
            task = (base_task_t*)malloc(sizeof(primality_test_task_t));
            if (task == NULL) {
                LOG_ERROR(
                    "Fatal Error: failed to allocate memory for primality test "
                    "task.");
                return NULL;
            }
            memset(task, 0, sizeof(primality_test_task_t));
            break;
        case FACTORIZE:
            task = (base_task_t*)malloc(sizeof(factorize_task_t));
            if (task == NULL) {
                LOG_ERROR(
                    "Fatal Error: failed to allocate memory for factorize "
                    "task.");
                return NULL;
            }
            memset(task, 0, sizeof(factorize_task_t));
            break;
        case UNKNOWN_TASK:
            LOG_ERROR("'%s' type received. Failed to create task.",
                      get_task_type_name(UNKNOWN_TASK));
            return NULL;
        default:
            LOG_ERROR("Argument error: not valid task type: %d", task_type);
            return NULL;
    }

    task->task_type = task_type;
    task->status = PENDING;
    return task;
}

void free_task(base_task_t** task) {
    if (task == NULL || *task == NULL) {
        return;
    }

    switch ((*task)->task_type) {
        case GCD: {
            gcd_task_t* gcd_task = (gcd_task_t*)*task;
            mpz_clear(gcd_task->a);
            mpz_clear(gcd_task->b);
            mpz_clear(gcd_task->gcd);
            break;
        }
        case LCM: {
            lcm_task_t* lcm_task = (lcm_task_t*)*task;
            mpz_clear(lcm_task->a);
            mpz_clear(lcm_task->b);
            mpz_clear(lcm_task->lcm);
            break;
        }
        case EXTENDED_GCD: {
            extended_euclidean_task_t* extended_euclidean_task =
                (extended_euclidean_task_t*)*task;
            mpz_clear(extended_euclidean_task->a);
            mpz_clear(extended_euclidean_task->b);
            mpz_clear(extended_euclidean_task->x);
            mpz_clear(extended_euclidean_task->y);
            mpz_clear(extended_euclidean_task->gcd);
            break;
        }
        case PRIMALITY_TEST: {
            primality_test_task_t* primality_test_task =
                (primality_test_task_t*)*task;
            mpz_clear(primality_test_task->input);
            break;
        }
        case FACTORIZE: {
            factorize_task_t* factorize_task = (factorize_task_t*)*task;
            mpz_clear(factorize_task->input);

            if (factorize_task->factors != NULL) {
                for (int i = 0; i < factorize_task->factor_count; i++) {
                    mpz_clear(factorize_task->factors[i]);
                }

                free(factorize_task->factors);
            }
            break;
        }
        case UNKNOWN_TASK:
            LOG_ERROR(
                "'%s' type received. Failed to release detailed resource. ID: "
                "%s",
                get_task_type_name(UNKNOWN_TASK), (*task)->task_id);
            break;
        default:
            LOG_ERROR(
                "Unhandled task type: %d. Failed to release detailed resource. "
                "ID: %s",
                (*task)->task_type, (*task)->task_id);
            break;
    }

    free(*task);
    *task = NULL;
}
