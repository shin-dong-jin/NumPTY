#pragma once

typedef enum ingress_status_e {
    INGRESS_SUCCESS = 0,
    INGRESS_TIMEOUT = 1,
    INGRESS_SHUTDOWN = 2,
    INGRESS_ERR_CONN = -1,
    INGRESS_ERR_PARSE = -2,
    INGRESS_ERR_FORMAT = -3,
    INGRESS_ERR_MEMORY = -4,
    INGRESS_ERR_NOT_FOUND = -5,
    INGRESS_ERR_ARGS = -6
} ingress_status_e;

struct worker_config_t;
struct redis_client_t;
struct base_task_t;

ingress_status_e ingress_fetch_task(struct worker_config_t* worker_config,
                                    struct redis_client_t* redis_client,
                                    struct base_task_t** task);