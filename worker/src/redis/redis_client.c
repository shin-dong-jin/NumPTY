#define _GNU_SOURCE

#include "redis/redis_client.h"

#include <hiredis/hiredis.h>

#include "common.h"
#include "logger.h"

redis_client_t* init_redis_client(const char* host,
                                  int port,
                                  const char* password) {
    if (host == NULL) {
        LOG_ERROR("Invalid argument: redis host is NULL.");
        return NULL;
    }

    if (port <= 0 || port > 65535) {
        LOG_ERROR("Invalid argument: redis port is out of range (%d)", port);
        return NULL;
    }

    if (password == NULL) {
        LOG_ERROR("Invalid argument: redis password is NULL.");
        return NULL;
    }

    redis_client_t* client = malloc(sizeof(redis_client_t));
    if (client == NULL) {
        LOG_ERROR("Connection error: cannot allocate memory for redis client.");
        return NULL;
    }

    client->host = strdup(host);
    if (client->host == NULL) {
        LOG_ERROR(
            "Connection error: cannot allocate memory for redis host string.");
        free(client);
        return NULL;
    }

    client->port = port;

    client->context = redisConnect(host, port);
    if (client->context == NULL || client->context->err) {
        if (client->context) {
            LOG_ERROR("Connection error: %s\n", client->context->errstr);
            redisFree(client->context);
        } else {
            LOG_ERROR(
                "Connection error: cannot allocate memory for redis context.");
        }

        free(client->host);
        free(client);
        return NULL;
    }

    if (password[0] != '\0') {
        redisReply* reply = redisCommand(client->context, "AUTH %s", password);

        if (reply == NULL || reply->type == REDIS_REPLY_ERROR) {
            LOG_ERROR("Redis AUTH failed: %s", reply ? reply->str : "no reply");

            if (reply) {
                freeReplyObject(reply);
            }
            redisFree(client->context);
            free(client->host);
            free(client);

            return NULL;
        }

        freeReplyObject(reply);
        LOG_INFO("Redis Authorized.");
    }

    LOG_INFO("Redis Client initialized.");

    return client;
}

void free_redis_client(redis_client_t* client) {
    if (!client) {
        LOG_INFO("Redis client is already closed or NULL.");
        return;
    }

    if (client->context) {
        redisFree(client->context);
    }
    free(client->host);
    free(client);

    LOG_INFO("Redis Client is closed.");
}