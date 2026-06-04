#include "transport/ingress.h"

#include <hiredis/hiredis.h>

#include "base/config.h"
#include "base/protocol.h"
#include "base/signal_handler.h"
#include "calculator/number_theory/exec_number_theory.h"
#include "common.h"
#include "logger.h"
#include "redis/redis_client.h"
#include "redis/redis_stream.h"

static ingress_status_e peek_task_type(redisReply* kv_pairs,
                                       task_type_e* task_type) {
    if (kv_pairs == NULL || task_type == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to peek_task_type.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->element == NULL) {
        LOG_ERROR("Fatal error: kv_pairs->element is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->elements % 2 != 0) {
        LOG_ERROR("Fatal error: kv_pairs->elements count is not even: %zu",
                  kv_pairs->elements);
        return INGRESS_ERR_ARGS;
    }

    for (size_t i = 0; i < kv_pairs->elements; i += 2) {
        if (strcmp(kv_pairs->element[i]->str, REDIS_KEY_TASK_TYPE) == 0) {
            char* value = kv_pairs->element[i + 1]->str;

            if (strcmp(value, get_task_type_name(GCD)) == 0) {
                *task_type = GCD;
                return INGRESS_SUCCESS;
            }

            if (strcmp(value, get_task_type_name(LCM)) == 0) {
                *task_type = LCM;
                return INGRESS_SUCCESS;
            }

            if (strcmp(value, get_task_type_name(EXTENDED_GCD)) == 0) {
                *task_type = EXTENDED_GCD;
                return INGRESS_SUCCESS;
            }

            if (strcmp(value, get_task_type_name(PRIMALITY_TEST)) == 0) {
                *task_type = PRIMALITY_TEST;
                return INGRESS_SUCCESS;
            }

            if (strcmp(value, get_task_type_name(FACTORIZE)) == 0) {
                *task_type = FACTORIZE;
                return INGRESS_SUCCESS;
            }

            LOG_ERROR("Argument error: unknown task_type: '%s'", value);
            *task_type = UNKNOWN_TASK;
            return INGRESS_ERR_ARGS;
        }
    }

    LOG_ERROR("Parse error: '%s' field not found.", REDIS_KEY_TASK_TYPE);
    return INGRESS_ERR_PARSE;
}

static ingress_status_e parse_base_task(redisReply* kv_pairs,
                                        base_task_t* task) {
    if (kv_pairs == NULL || task == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to parse_base_task.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->element == NULL) {
        LOG_ERROR("Fatal error: kv_pairs->element is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->elements % 2 != 0) {
        LOG_ERROR("Fatal error: kv_pairs->elements count is not even: %zu",
                  kv_pairs->elements);
        return INGRESS_ERR_ARGS;
    }

    int field_found = 0;

    for (size_t i = 0; i < kv_pairs->elements; i += 2) {
        char* key = kv_pairs->element[i]->str;
        char* value = kv_pairs->element[i + 1]->str;

        if (strcmp(key, REDIS_KEY_TASK_ID) == 0) {
            strncpy(task->task_id, value, sizeof(task->task_id) - 1);
            task->task_id[sizeof(task->task_id) - 1] = '\0';
            field_found++;
            continue;
        }

        if (strcmp(key, REDIS_KEY_TASK_TYPE) == 0) {
            field_found++;
            continue;
        }
    }

    if (field_found != 2) {
        LOG_ERROR("Fatal error: missing base task field.");
        return INGRESS_ERR_PARSE;
    }

    return INGRESS_SUCCESS;
}

static ingress_status_e parse_gcd_task(redisReply* kv_pairs, gcd_task_t* task) {
    if (kv_pairs == NULL || task == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to parse_gcd_task.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->element == NULL) {
        LOG_ERROR("Fatal error: kv_pairs->element is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->elements % 2 != 0) {
        LOG_ERROR("Fatal error: kv_pairs->elements count is not even: %zu",
                  kv_pairs->elements);
        return INGRESS_ERR_ARGS;
    }

    int field_found = 0;
    mpz_init(task->a);
    mpz_init(task->b);
    mpz_init(task->gcd);

    for (size_t i = 0; i < kv_pairs->elements; i += 2) {
        char* key = kv_pairs->element[i]->str;
        char* value = kv_pairs->element[i + 1]->str;

        if (strcmp(key, REDIS_KEY_INPUT_A) == 0) {
            if (mpz_set_str(task->a, value, 10) != 0) {
                LOG_ERROR("Parse error: invalid numeric format: '%s'", value);
                mpz_clear(task->a);
                mpz_clear(task->b);
                mpz_clear(task->gcd);
                return INGRESS_ERR_PARSE;
            }
            field_found++;
            continue;
        }

        if (strcmp(key, REDIS_KEY_INPUT_B) == 0) {
            if (mpz_set_str(task->b, value, 10) != 0) {
                LOG_ERROR("Parse error: invalid numeric format: '%s'", value);
                mpz_clear(task->a);
                mpz_clear(task->b);
                mpz_clear(task->gcd);
                return INGRESS_ERR_PARSE;
            }
            field_found++;
            continue;
        }
    }

    if (field_found != 2) {
        LOG_ERROR("Fatal error: missing input field.");
        mpz_clear(task->a);
        mpz_clear(task->b);
        mpz_clear(task->gcd);
        return INGRESS_ERR_PARSE;
    }

    return INGRESS_SUCCESS;
}

static ingress_status_e parse_lcm_task(redisReply* kv_pairs, lcm_task_t* task) {
    if (kv_pairs == NULL || task == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to parse_gcd_task.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->element == NULL) {
        LOG_ERROR("Fatal error: kv_pairs->element is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->elements % 2 != 0) {
        LOG_ERROR("Fatal error: kv_pairs->elements count is not even: %zu",
                  kv_pairs->elements);
        return INGRESS_ERR_ARGS;
    }

    int field_found = 0;
    mpz_init(task->a);
    mpz_init(task->b);
    mpz_init(task->lcm);

    for (size_t i = 0; i < kv_pairs->elements; i += 2) {
        char* key = kv_pairs->element[i]->str;
        char* value = kv_pairs->element[i + 1]->str;

        if (strcmp(key, REDIS_KEY_INPUT_A) == 0) {
            if (mpz_set_str(task->a, value, 10) != 0) {
                LOG_ERROR("Parse error: invalid numeric format: '%s'", value);
                mpz_clear(task->a);
                mpz_clear(task->b);
                mpz_clear(task->lcm);
                return INGRESS_ERR_PARSE;
            }
            field_found++;
            continue;
        }

        if (strcmp(key, REDIS_KEY_INPUT_B) == 0) {
            if (mpz_set_str(task->b, value, 10) != 0) {
                LOG_ERROR("Parse error: invalid numeric format: '%s'", value);
                mpz_clear(task->a);
                mpz_clear(task->b);
                mpz_clear(task->lcm);
                return INGRESS_ERR_PARSE;
            }
            field_found++;
            continue;
        }
    }

    if (field_found != 2) {
        LOG_ERROR("Fatal error: missing input field.");
        mpz_clear(task->a);
        mpz_clear(task->b);
        mpz_clear(task->lcm);
        return INGRESS_ERR_PARSE;
    }

    return INGRESS_SUCCESS;
}

static ingress_status_e parse_extended_euclidean(
    redisReply* kv_pairs,
    extended_euclidean_task_t* task) {
    if (kv_pairs == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to "
            "parse_extended_euclidean.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->element == NULL) {
        LOG_ERROR("Fatal error: kv_pairs->element is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->elements % 2 != 0) {
        LOG_ERROR("Fatal error: kv_pairs->elements count is not even: %zu",
                  kv_pairs->elements);
        return INGRESS_ERR_ARGS;
    }

    int field_found = 0;
    mpz_init(task->a);
    mpz_init(task->b);
    mpz_init(task->x);
    mpz_init(task->y);
    mpz_init(task->gcd);

    for (size_t i = 0; i < kv_pairs->elements; i += 2) {
        char* key = kv_pairs->element[i]->str;
        char* value = kv_pairs->element[i + 1]->str;

        if (strcmp(key, REDIS_KEY_INPUT_A) == 0) {
            if (mpz_set_str(task->a, value, 10) != 0) {
                LOG_ERROR("Parse error: invalid numeric format: '%s'", value);
                mpz_clear(task->a);
                mpz_clear(task->b);
                mpz_clear(task->x);
                mpz_clear(task->y);
                mpz_clear(task->gcd);
                return INGRESS_ERR_PARSE;
            }
            field_found++;
            continue;
        }

        if (strcmp(key, REDIS_KEY_INPUT_B) == 0) {
            if (mpz_set_str(task->b, value, 10) != 0) {
                LOG_ERROR("Parse error: invalid numeric format: '%s'", value);
                mpz_clear(task->a);
                mpz_clear(task->b);
                mpz_clear(task->x);
                mpz_clear(task->y);
                mpz_clear(task->gcd);
                return INGRESS_ERR_PARSE;
            }
            field_found++;
            continue;
        }
    }

    if (field_found != 2) {
        LOG_ERROR("Fatal error: missing input field.");
        mpz_clear(task->a);
        mpz_clear(task->b);
        mpz_clear(task->x);
        mpz_clear(task->y);
        mpz_clear(task->gcd);
        return INGRESS_ERR_PARSE;
    }

    return INGRESS_SUCCESS;
}

static ingress_status_e parse_primality_test_task(redisReply* kv_pairs,
                                                  primality_test_task_t* task) {
    if (kv_pairs == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to "
            "parse_primality_test_task.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->element == NULL) {
        LOG_ERROR("Fatal error: kv_pairs->element is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->elements % 2 != 0) {
        LOG_ERROR("Fatal error: kv_pairs->elements count is not even: %zu",
                  kv_pairs->elements);
        return INGRESS_ERR_ARGS;
    }

    int field_found = 0;
    mpz_init(task->input);

    for (size_t i = 0; i < kv_pairs->elements; i += 2) {
        char* key = kv_pairs->element[i]->str;
        char* value = kv_pairs->element[i + 1]->str;

        if (strcmp(key, REDIS_KEY_INPUT) == 0) {
            if (mpz_set_str(task->input, value, 10) != 0) {
                LOG_ERROR("Parse error: invalid numeric format: '%s'", value);
                mpz_clear(task->input);
                return INGRESS_ERR_PARSE;
            }
            field_found++;
            continue;
        }
    }

    if (field_found != 1) {
        LOG_ERROR("Fatal error: missing input field.");
        mpz_clear(task->input);
        return INGRESS_ERR_PARSE;
    }

    return INGRESS_SUCCESS;
}

static ingress_status_e parse_factorize_task(redisReply* kv_pairs,
                                             factorize_task_t* task) {
    if (kv_pairs == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to parse_factorize_task.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->element == NULL) {
        LOG_ERROR("Fatal error: kv_pairs->element is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (kv_pairs->elements % 2 != 0) {
        LOG_ERROR("Fatal error: kv_pairs->elements count is not even: %zu",
                  kv_pairs->elements);
        return INGRESS_ERR_ARGS;
    }

    int field_found = 0;

    mpz_init(task->input);

    for (size_t i = 0; i < kv_pairs->elements; i += 2) {
        char* key = kv_pairs->element[i]->str;
        char* value = kv_pairs->element[i + 1]->str;

        if (strcmp(key, REDIS_KEY_INPUT) == 0) {
            if (mpz_set_str(task->input, value, 10) != 0) {
                LOG_ERROR("Parse error: invalid numeric format: '%s'", value);
                mpz_clear(task->input);
                return INGRESS_ERR_PARSE;
            }
            field_found++;
            continue;
        }
    }

    if (field_found != 1) {
        LOG_ERROR("Fatal error: missing input field.");
        mpz_clear(task->input);
        return INGRESS_ERR_PARSE;
    }

    return INGRESS_SUCCESS;
}

static ingress_status_e deserialize_task(redisReply* reply,
                                         base_task_t** out_task) {
    if (reply == NULL || out_task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to deserialize_task.");
        return INGRESS_ERR_ARGS;
    }

    if (reply->type != REDIS_REPLY_ARRAY || reply->elements == 0) {
        LOG_ERROR(
            "Fatal error: Not matched reply type or elements on redis reply.");
        return INGRESS_ERR_ARGS;
    }

    redisReply* stream_data = reply->element[0];
    if (stream_data->type != REDIS_REPLY_ARRAY || stream_data->elements < 2) {
        LOG_ERROR(
            "Fatal error: Not matched reply type or elements on stream data.");
        return INGRESS_ERR_ARGS;
    }

    redisReply* messages = stream_data->element[1];
    if (messages->type != REDIS_REPLY_ARRAY || messages->elements == 0) {
        LOG_ERROR(
            "Fatal error: Not matched reply type or elements on messages.");
        return INGRESS_ERR_ARGS;
    }

    redisReply* msg = messages->element[0];
    if (msg->type != REDIS_REPLY_ARRAY || msg->elements < 2) {
        LOG_ERROR(
            "Fatal error: Not matched reply type or elements on message.");
        return INGRESS_ERR_ARGS;
    }

    redisReply* msg_id = msg->element[0];
    redisReply* kv_pairs = msg->element[1];

    task_type_e task_type = UNKNOWN_TASK;
    ingress_status_e peek_task_result = peek_task_type(kv_pairs, &task_type);

    if (peek_task_result != INGRESS_SUCCESS) {
        return peek_task_result;
    }

    base_task_t* task = create_task(task_type);

    if (task == NULL) {
        LOG_ERROR("Error occurred while creating task.");
        return INGRESS_ERR_MEMORY;
    }

    strncpy(task->stream_msg_id, msg_id->str, sizeof(task->stream_msg_id) - 1);
    task->stream_msg_id[sizeof(task->stream_msg_id) - 1] = '\0';

    ingress_status_e parse_task_result = parse_base_task(kv_pairs, task);

    if (parse_task_result != INGRESS_SUCCESS) {
        LOG_ERROR("Error occurred while parsing base task.");
        free(task);
        return parse_task_result;
    }

    switch (task_type) {
        case GCD:
            parse_task_result = parse_gcd_task(kv_pairs, (gcd_task_t*)task);
            break;
        case LCM:
            parse_task_result = parse_lcm_task(kv_pairs, (lcm_task_t*)task);
            break;
        case EXTENDED_GCD:
            parse_task_result = parse_extended_euclidean(
                kv_pairs, (extended_euclidean_task_t*)task);
            break;
        case PRIMALITY_TEST:
            parse_task_result = parse_primality_test_task(
                kv_pairs, (primality_test_task_t*)task);
            break;
        case FACTORIZE:
            parse_task_result =
                parse_factorize_task(kv_pairs, (factorize_task_t*)task);
            break;
        default:
            LOG_ERROR("Argument error: not valid task type '%s'",
                      get_task_type_name(task_type));
            free(task);
            return INGRESS_ERR_ARGS;
    }

    if (parse_task_result != INGRESS_SUCCESS) {
        LOG_ERROR("Error occurred while parsing type task.");
        free(task);
        return parse_task_result;
    }

    *out_task = task;
    return INGRESS_SUCCESS;
}

ingress_status_e ingress_fetch_task(worker_config_t* worker_config,
                                    redis_client_t* redis_client,
                                    base_task_t** task) {
    if (worker_config == NULL || redis_client == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer is passed to ingress_fetch_task.");
        return INGRESS_ERR_ARGS;
    }

    if (worker_config->stream_request == NULL ||
        worker_config->stream_request[0] == '\0') {
        LOG_ERROR("Fatal error: worker_config->stream_request is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (worker_config->consumer_group == NULL ||
        worker_config->consumer_group[0] == '\0') {
        LOG_ERROR("Fatal error: worker_config->consumer_group is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (worker_config->consumer_name == NULL ||
        worker_config->consumer_name[0] == '\0') {
        LOG_ERROR("Fatal error: worker_config->consumer_name is NULL.");
        return INGRESS_ERR_ARGS;
    }

    if (worker_config->block_timeout_ms < 0) {
        LOG_ERROR(
            "Fatal error: worker_config->block_timeout cannot be negative.");
        return INGRESS_ERR_ARGS;
    }

    if (redis_client->context == NULL) {
        LOG_ERROR("Fatal error: redis_client->context is NULL.");
        return INGRESS_ERR_ARGS;
    }

    redisReply* reply = NULL;
    redis_status_e redis_status = xreadgroup_redis_stream(
        redis_client, worker_config->stream_request,
        worker_config->consumer_group, worker_config->consumer_name,
        worker_config->block_timeout_ms, &reply);

    if (redis_status == REDIS_TIMEOUT) {
        if (!keep_running) {
            return INGRESS_SHUTDOWN;
        }

        return INGRESS_TIMEOUT;
    }

    if (redis_status != REDIS_SUCCESS) {
        LOG_ERROR(
            "Connection error: connection refused while waiting redis "
            "queue.");

        return INGRESS_ERR_CONN;
    }

    ingress_status_e deserialize_result = deserialize_task(reply, task);

    freeReplyObject(reply);

    if (deserialize_result != INGRESS_SUCCESS) {
        LOG_ERROR("Error occurred while deserializing task.");
        return deserialize_result;
    }

    return INGRESS_SUCCESS;
}