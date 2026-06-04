#pragma once

typedef struct redis_pair_t {
    const char* key;
    const char* value;
} redis_pair_t;

enum redis_status_e;
struct redis_client_t;
struct redisReply;

enum redis_status_e setup_consumer_group(struct redis_client_t* client, const char* stream_request,
                                         const char* consumer_group);

enum redis_status_e xreadgroup_redis_stream(struct redis_client_t* client, const char* stream_name,
                                            const char* group_name, const char* consumer_name,
                                            int timeout, struct redisReply** reply);

enum redis_status_e xack_redis_stream(struct redis_client_t* client, const char* stream_name,
                                      const char* group_name, const char* msg_id);

enum redis_status_e xadd_redis_stream(struct redis_client_t* client, const char* stream_name,
                                      redis_pair_t* redis_pairs, int pair_size);