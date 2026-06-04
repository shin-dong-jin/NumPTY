#include "redis/redis_queue.h"

#include <hiredis/hiredis.h>

#include "common.h"
#include "logger.h"
#include "redis/redis_client.h"

redis_status_e rpop_redis_queue(redis_client_t* client, const char* queue_name, char** reply_str) {
    if (client == NULL || queue_name == NULL || reply_str == NULL) {
        LOG_ERROR("Invalid argument: NULL pointer is passed to rpop_redis_queue.");
        return REDIS_ERR_ARGS;
    }

    if (client->context == NULL) {
        LOG_ERROR("Fatal error: client->context is NULL.");
        return REDIS_ERR_ARGS;
    }

    *reply_str = NULL;

    redisReply* reply = redisCommand(client->context, "RPOP %s", queue_name);

    if (reply == NULL) {
        LOG_ERROR("Fatal error: Redis connection lost.");
        return REDIS_ERR_ARGS;
    }

    if (reply->type == REDIS_REPLY_STRING) {
        *reply_str = strdup(reply->str);
        freeReplyObject(reply);

        if (*reply_str == NULL) {
            LOG_ERROR("Memory allocation failed in strdup to reply_str.");
            return REDIS_ERR_MEMORY;
        }

        return REDIS_SUCCESS;
    }

    freeReplyObject(reply);
    return REDIS_ERR_TYPE;
}

redis_status_e brpop_redis_queue(redis_client_t* client, const char* queue_name, int timeout,
                                 char** reply_str) {
    if (client == NULL || queue_name == NULL || reply_str == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to brpop_redis_queue.");
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

    *reply_str = NULL;

    redisReply* reply = redisCommand(client->context, "BRPOP %s %d", queue_name, timeout);

    if (reply == NULL) {
        LOG_ERROR("Fatal error: Redis connection lost.");
        return REDIS_ERR_CONN;
    }

    if (reply->type == REDIS_REPLY_NIL) {
        freeReplyObject(reply);
        return REDIS_TIMEOUT;
    }

    if (reply->type == REDIS_REPLY_ARRAY && reply->elements == 2) {
        *reply_str = strdup(reply->element[1]->str);
        freeReplyObject(reply);

        if (*reply_str == NULL) {
            LOG_ERROR("Memory allocation failed in strdup to reply_str.");
            return REDIS_ERR_MEMORY;
        }

        return REDIS_SUCCESS;
    }

    freeReplyObject(reply);
    return REDIS_ERR_TYPE;
}

redis_status_e lpush_redis_queue(redis_client_t* client, const char* queue_name,
                                 const char* message) {
    if (client == NULL || queue_name == NULL || message == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to lpush_redis_queue.");
        return REDIS_ERR_ARGS;
    }

    if (client->context == NULL) {
        LOG_ERROR("Fatal error: client->context is NULL.");
        return REDIS_ERR_ARGS;
    }

    redisReply* reply = redisCommand(client->context, "LPUSH %s %s", queue_name, message);

    if (reply == NULL) {
        LOG_ERROR("Fatal error: Redis connection lost.");
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