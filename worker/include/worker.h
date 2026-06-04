#pragma once

typedef enum worker_status_e { WORKER_SUCCESS = 0, WORKER_FAILED = -1 } worker_status_e;

struct worker_config_t;
struct redis_client_t;

worker_status_e run(struct worker_config_t* worker_config, struct redis_client_t* redis_client);