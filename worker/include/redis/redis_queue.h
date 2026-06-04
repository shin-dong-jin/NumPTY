#pragma once

enum redis_status_e;
struct redis_client_t;

enum redis_status_e rpop_redis_queue(struct redis_client_t* client, const char* queue_name,
                                     char** reply_str);

enum redis_status_e brpop_redis_queue(struct redis_client_t* client, const char* queue_name,
                                      int timeout, char** reply_str);

enum redis_status_e lpush_redis_queue(struct redis_client_t* client, const char* queue_name,
                                      const char* message);