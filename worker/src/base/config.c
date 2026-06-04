#define _GNU_SOURCE
#include "base/config.h"

#include <unistd.h>

#include "common.h"
#include "logger.h"

static char* get_env_str(const char* key, const char* default_value) {
    const char* value = getenv(key);
    return strdup(value ? value : default_value);
}

static int get_env_int(const char* key, int default_value) {
    const char* value_str = getenv(key);

    if (!value_str) {
        return default_value;
    }

    char* endptr;
    long value_long = strtol(value_str, &endptr, 10);

    if (endptr == value_str) {
        LOG_WARN("Invalid integer input for %s, using default %d", key,
                 default_value);
        return default_value;
    }

    return (int)value_long;
}

static char* generate_consumer_name() {
    char hostname[256];

    if (gethostname(hostname, sizeof(hostname)) != 0) {
        strncpy(hostname, "unknown", sizeof(hostname) - 1);
        hostname[sizeof(hostname) - 1] = '\0';
    }

    pid_t pid = getpid();

    size_t buffer_size = strlen(hostname) + 32;
    char* consumer_name = (char*)malloc(buffer_size);

    if (consumer_name == NULL) {
        return NULL;
    }

    snprintf(consumer_name, buffer_size, "%s:%d", hostname, pid);

    return consumer_name;
}

void print_banner() {
    char* banner_path = get_env_str("BANNER_PATH", "resource/banner.logo");
    FILE* fp = fopen(banner_path, "r");
    char buffer[256];

    if (fp == NULL) {
        fprintf(stdout, "NumPTY Worker Starting...\n");
        fprintf(stdout, "                (v.1.0.0)\n");
        free(banner_path);
        return;
    }

    while (fgets(buffer, sizeof(buffer), fp) != NULL) {
        fprintf(stdout, "%s", buffer);
    }

    fclose(fp);
    free(banner_path);
}

config_status_e load_worker_config(worker_config_t* worker_config) {
    if (worker_config == NULL) {
        LOG_ERROR("Invalid argument: worker config struct is NULL.");
        return CONFIG_ERR_ARGS;
    }

    *worker_config = (worker_config_t){
        .log_level = get_env_int("TARGET_LOG_LEVEL", 1),
        .redis_host = get_env_str("REDIS_HOST", "127.0.0.1"),
        .redis_port = get_env_int("REDIS_PORT", 6379),
        .redis_password = get_env_str("REDIS_PASSWORD", ""),
        .consumer_name = generate_consumer_name(),
        .consumer_group = get_env_str("CONSUMER_GROUP", "cg-worker"),
        .stream_request = get_env_str("STREAM_REQUEST", "numpty:was:request"),
        .stream_response =
            get_env_str("STREAM_RESPONSE", "numpty:worker:response"),
        .block_timeout_ms = get_env_int("BLOCK_TIMEOUT_MS", 1)};

    if (worker_config->redis_host == NULL ||
        worker_config->redis_password == NULL ||
        worker_config->consumer_name == NULL ||
        worker_config->consumer_group == NULL ||
        worker_config->stream_request == NULL ||
        worker_config->stream_response == NULL) {
        LOG_ERROR("Fatal error: cannot allocate memory for worker config.");
        free_worker_config(worker_config);
        return CONFIG_ERR_MEMORY;
    }

    if (worker_config->redis_port <= 0 || worker_config->redis_port > 65535) {
        LOG_ERROR("Fatal error: redis port is out of range(%d).",
                  worker_config->redis_port);
        free_worker_config(worker_config);
        return CONFIG_ERR_ARGS;
    }

    if (worker_config->block_timeout_ms < 0) {
        LOG_ERROR("Fatal error: timeout cannot be negative.");
        free_worker_config(worker_config);
        return CONFIG_ERR_ARGS;
    }

    if (worker_config->log_level < LOG_LEVEL_DEBUG ||
        worker_config->log_level > LOG_LEVEL_ERROR) {
        LOG_WARN("Target log level is out of range(%d).",
                 worker_config->log_level);
        LOG_WARN("Log level set to default level(INFO).");
        worker_config->log_level = LOG_LEVEL_INFO;
        return CONFIG_WARN_ARGS;
    }

    unsetenv("REDIS_PASSWORD");

    LOG_INFO("Worker Config loaded.");
    return CONFIG_SUCCESS;
}

void free_worker_config(worker_config_t* worker_config) {
    if (worker_config == NULL) {
        LOG_INFO("Worker Config is already closed or NULL.");
        return;
    }

    free(worker_config->redis_host);
    free(worker_config->redis_password);
    free(worker_config->consumer_name);
    free(worker_config->consumer_group);
    free(worker_config->stream_request);
    free(worker_config->stream_response);

    worker_config->redis_host = NULL;
    worker_config->redis_password = NULL;
    worker_config->consumer_name = NULL;
    worker_config->consumer_group = NULL;
    worker_config->stream_request = NULL;
    worker_config->stream_response = NULL;

    LOG_INFO("Worker Config is closed.");
}