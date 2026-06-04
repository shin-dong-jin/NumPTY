#pragma once

typedef enum config_status_e {
    CONFIG_SUCCESS = 0,
    CONFIG_WARN_ARGS = 1,
    CONFIG_ERR_MEMORY = -1,
    CONFIG_ERR_ARGS = -2
} config_status_e;

typedef struct worker_config_t {
    int log_level;
    char* redis_host;
    int redis_port;
    char* redis_password;
    char* consumer_name;
    char* consumer_group;
    char* stream_request;
    char* stream_response;
    int block_timeout_ms;
} worker_config_t;

void print_banner();

config_status_e load_worker_config(worker_config_t* worker_config);

void free_worker_config(worker_config_t* worker_config);
