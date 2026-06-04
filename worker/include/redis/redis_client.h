#pragma once

typedef enum redis_status_e {
    REDIS_SUCCESS = 0,
    REDIS_TIMEOUT = 1,
    REDIS_ERR_CONN = -1,
    REDIS_ERR_CMD = -2,
    REDIS_ERR_MEMORY = -3,
    REDIS_ERR_ARGS = -4,
    REDIS_ERR_TYPE = -5,
    REDIS_ERR_CG_SETUP = -6
} redis_status_e;

struct redisContext;

typedef struct redis_client_t {
    struct redisContext* context;
    char* host;
    int port;
} redis_client_t;

redis_client_t* init_redis_client(const char* host,
                                  int port,
                                  const char* password);

void free_redis_client(redis_client_t* client);