#include "redis/redis_stream.h"

#include <hiredis/hiredis.h>

#include "common.h"
#include "logger.h"
#include "redis/redis_client.h"

redis_status_e setup_consumer_group(redis_client_t* client, const char* stream_request,
                                    const char* consumer_group) {
    if (client == NULL || stream_request == NULL || consumer_group == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer is passed to setup_consumer_group.");
        return REDIS_ERR_ARGS;
    }

    if (client->context == NULL) {
        LOG_ERROR("Fatal error: client->context is NULL.");
        return REDIS_ERR_ARGS;
    }

    redisReply* reply = redisCommand(client->context, "XGROUP CREATE %s %s $ MKSTREAM",
                                     stream_request, consumer_group);

    if (reply == NULL) {
        LOG_ERROR("Fatal Error: Redis connection lost.");
        return REDIS_ERR_CONN;
    }

    if (reply->type == REDIS_REPLY_ERROR && strstr(reply->str, "BUSYGROUP") != NULL) {
        LOG_INFO("Consumer group '%s' already exists on %s.", consumer_group, stream_request);
        freeReplyObject(reply);
        return REDIS_SUCCESS;
    }

    if (reply->type != REDIS_REPLY_ERROR) {
        LOG_INFO("Created consumer group '%s' on %s.", consumer_group, stream_request);
        freeReplyObject(reply);
        return REDIS_SUCCESS;
    }

    LOG_ERROR("Error occurred while creating consumer group '%s': %s.", consumer_group, reply->str);
    freeReplyObject(reply);
    return REDIS_ERR_CG_SETUP;
}

redis_status_e xreadgroup_redis_stream(redis_client_t* client, const char* stream_name,
                                       const char* group_name, const char* consumer_name,
                                       int timeout, redisReply** reply) {
    if (client == NULL || stream_name == NULL || group_name == NULL || consumer_name == NULL ||
        reply == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer is passed to xreadgroup_redis_stream.");
        return REDIS_ERR_ARGS;
    }

    if (client->context == NULL) {
        LOG_ERROR("Fatal error: client->context is NULL.");
        return REDIS_ERR_ARGS;
    }

    if (timeout < 0) {
        LOG_ERROR("Fatal error: timeout cannot be negative.");
        return REDIS_ERR_ARGS;
    }

    *reply = redisCommand(client->context, "XREADGROUP GROUP %s %s BLOCK %d COUNT 1 STREAMS %s >",
                          group_name, consumer_name, timeout, stream_name);

    if (*reply == NULL) {
        return REDIS_ERR_CONN;
    }

    if ((*reply)->type == REDIS_REPLY_NIL) {
        freeReplyObject(*reply);
        *reply = NULL;
        return REDIS_TIMEOUT;
    }

    return REDIS_SUCCESS;
}

redis_status_e xack_redis_stream(redis_client_t* client, const char* stream_name,
                                 const char* group_name, const char* msg_id) {
    if (client == NULL || stream_name == NULL || group_name == NULL || msg_id == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer is passed to xack_redis_stream.");
        return REDIS_ERR_ARGS;
    }

    if (client->context == NULL) {
        LOG_ERROR("Fatal error: client->context is NULL.");
        return REDIS_ERR_ARGS;
    }

    redisReply* reply =
        redisCommand(client->context, "XACK %s %s %s", stream_name, group_name, msg_id);

    if (reply == NULL) {
        LOG_ERROR("Fatal Error: Redis connection lost.");
        return REDIS_ERR_CONN;
    }

    if (reply->type == REDIS_REPLY_INTEGER) {
        freeReplyObject(reply);
        return REDIS_SUCCESS;
    }

    LOG_ERROR("Fatal error: XACK failed. ID: %s, msg: %s", msg_id,
              reply->type == REDIS_REPLY_ERROR ? reply->str : "Unknown error");
    freeReplyObject(reply);
    return REDIS_ERR_CMD;
}

redis_status_e xadd_redis_stream(redis_client_t* client, const char* stream_name,
                                 redis_pair_t* redis_pairs, int pair_size) {
    if (client == NULL || stream_name == NULL || redis_pairs == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer is passed to xadd_redis_stream.");
        return REDIS_ERR_ARGS;
    }

    if (client->context == NULL) {
        LOG_ERROR("Fatal error: client->context is NULL.");
        return REDIS_ERR_ARGS;
    }

    int argc = 2 * pair_size + 6;
    const char** argv = malloc(sizeof(char*) * argc);

    if (argv == NULL) {
        LOG_ERROR("Memory allocation failed in malloc to argv");
        return REDIS_ERR_MEMORY;
    }

    argv[0] = "XADD";
    argv[1] = stream_name;
    argv[2] = "MAXLEN";
    argv[3] = "~";
    argv[4] = "1000";
    argv[5] = "*";

    int idx = 6;
    for (int i = 0; i < pair_size; i++) {
        argv[idx++] = redis_pairs[i].key;
        argv[idx++] = redis_pairs[i].value;
    }

    redisReply* reply = redisCommandArgv(client->context, argc, argv, NULL);
    free(argv);

    if (reply == NULL) {
        LOG_ERROR("Fatal Error: Redis connection lost.");
        return REDIS_ERR_CONN;
    }

    if (reply->type == REDIS_REPLY_ERROR) {
        LOG_ERROR("Redis Reply Error: %s", reply->str);
        freeReplyObject(reply);
        return REDIS_ERR_CMD;
    }

    freeReplyObject(reply);

    return REDIS_SUCCESS;
}