#pragma once

typedef enum egress_status_e {
    EGRESS_SUCCESS = 0,
    EGRESS_ERR_CONN = -1,
    EGRESS_ERR_FORMAT = -2,
    EGRESS_ERR_MEMORY = -3,
    EGRESS_ERR_ARGS = -4
} egress_status_e;

struct worker_config_t;
struct redis_client_t;
struct base_task_t;

egress_status_e egress_send_status(struct worker_config_t* worker_config,
                                   struct redis_client_t* redis_client, struct base_task_t* task);

egress_status_e egress_send_result(struct worker_config_t* worekr_config,
                                   struct redis_client_t* redis_client, struct base_task_t* task);

egress_status_e egress_send_xack(struct worker_config_t* worker_config,
                                 struct redis_client_t* redis_client, struct base_task_t* task);