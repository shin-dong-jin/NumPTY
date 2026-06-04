#define _GNU_SOURCE

#include "base/config.h"
#include "base/signal_handler.h"
#include "common.h"
#include "logger.h"
#include "redis/redis_client.h"
#include "redis/redis_stream.h"
#include "worker.h"

int main(int argc, char** argv) {
    struct timespec start, end;
    clock_gettime(CLOCK_MONOTONIC, &start);

    print_banner();

    (void)argc;
    if (init_logger(argv[0]) != LOGGER_SUCCESS) {
        LOG_ERROR("Failed to initialize logger.");
        return EXIT_FAILURE;
    }

    worker_config_t worker_config = {0};
    if (load_worker_config(&worker_config) != CONFIG_SUCCESS) {
        LOG_ERROR("Failed to load Worker Config. Terminating worker.");
        return EXIT_FAILURE;
    }

    if (set_log_level(worker_config.log_level) != LOGGER_SUCCESS) {
        LOG_WARN(
            "Invalid target log level provided in config. Falling back to "
            "default level (INFO).");
    } else {
        switch (worker_config.log_level) {
            case LOG_LEVEL_DEBUG:
                LOG_DEBUG("Target Log Level set to %s",
                          get_log_level_name(worker_config.log_level));
                break;
            case LOG_LEVEL_INFO:
                LOG_INFO("Target Log Level set to %s",
                         get_log_level_name(worker_config.log_level));
                break;
            case LOG_LEVEL_WARN:
                LOG_WARN("Target Log Level set to %s",
                         get_log_level_name(worker_config.log_level));
                break;
            case LOG_LEVEL_ERROR:
                LOG_ERROR("Target Log Level set to %s",
                          get_log_level_name(worker_config.log_level));
                break;
        }
    }

    if (register_shutdown_handlers() != SIGNAL_SUCCESS) {
        LOG_ERROR("Failed to register Signal Handler. Terminating worker.");
        free_worker_config(&worker_config);
        return EXIT_FAILURE;
    }

    redis_client_t* redis_client =
        init_redis_client(worker_config.redis_host, worker_config.redis_port,
                          worker_config.redis_password);

    explicit_bzero(worker_config.redis_password,
                   strlen(worker_config.redis_password));

    if (redis_client == NULL) {
        LOG_ERROR("Failed to initialize Redis Client. Terminating worker.");
        free_worker_config(&worker_config);
        return EXIT_FAILURE;
    }

    if (setup_consumer_group(redis_client, worker_config.stream_request,
                             worker_config.consumer_group) != REDIS_SUCCESS) {
        LOG_ERROR("Failed to setup Consumer Group. Terminating worker.");
        LOG_ERROR("Releasing resources...");
        free_worker_config(&worker_config);
        free_redis_client(redis_client);
        LOG_INFO("All resources are released successfully.");
        return EXIT_FAILURE;
    }

    clock_gettime(CLOCK_MONOTONIC, &end);
    double duration = (end.tv_sec - start.tv_sec) * 1000.0 +
                      (end.tv_nsec - start.tv_nsec) / 1000000.0;

    LOG_INFO("Worker started successfully in %dms.", (int)duration);

    if (run(&worker_config, redis_client) != WORKER_SUCCESS) {
        LOG_ERROR(
            "Worker terminated abnormally. Unexpected exit of loop may lead to "
            "data loss.");
        LOG_ERROR("Releasing resources...");
        free_worker_config(&worker_config);
        free_redis_client(redis_client);
        LOG_INFO("All resources are released successfully.");
        return EXIT_FAILURE;
    }

    LOG_INFO("Shutdown signal received: Releasing resources...");

    free_worker_config(&worker_config);
    free_redis_client(redis_client);

    LOG_INFO("All resources are released successfully.");

    return EXIT_SUCCESS;
}