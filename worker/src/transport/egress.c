#include "transport/egress.h"

#include "base/config.h"
#include "base/protocol.h"
#include "common.h"
#include "logger.h"
#include "redis/redis_client.h"
#include "redis/redis_stream.h"

static egress_status_e egress_send_gcd_result(worker_config_t* worker_config,
                                              redis_client_t* redis_client,
                                              gcd_task_t* task) {
    if (worker_config == NULL || redis_client == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to "
            "egress_send_gcd_result.");
        return EGRESS_ERR_ARGS;
    }

    if (worker_config->stream_response == NULL ||
        worker_config->stream_response[0] == '\0') {
        LOG_ERROR("worker_config->stream_response is NULL.");
        return EGRESS_ERR_ARGS;
    }

    if (task->base.task_id[0] == '\0') {
        LOG_ERROR("task->base.task_id is blank.");
        return EGRESS_ERR_ARGS;
    }

    char* a_str = mpz_get_str(NULL, 10, task->a);
    if (a_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for a_str");
        return EGRESS_ERR_MEMORY;
    }

    char* b_str = mpz_get_str(NULL, 10, task->b);
    if (b_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for b_str");
        return EGRESS_ERR_MEMORY;
    }

    char* gcd_str = mpz_get_str(NULL, 10, task->gcd);
    if (gcd_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for gcd_str");
        return EGRESS_ERR_MEMORY;
    }

    if (task->base.algorithm[0] == '\0') {
        LOG_WARN("Fatal error: task->base.algorithm is blank.");
    }

    char elapsed_ms_str[32];
    snprintf(elapsed_ms_str, sizeof(elapsed_ms_str), "%.3f",
             task->base.elapsed_time_ms);
    elapsed_ms_str[sizeof(elapsed_ms_str) - 1] = '\0';

    const char* status_str = get_status_name(task->base.status);
    if (status_str == NULL || strcmp(status_str, "UNKNOWN") == 0 ||
        strcmp(status_str, "UNHANDLED") == 0) {
        LOG_ERROR("Error: unknown status code. (%d)", task->base.status);
        status_str = "UNKNOWN";
    }

    redis_pair_t redis_pairs[] = {
        {REDIS_KEY_TASK_ID, task->base.task_id},
        {REDIS_KEY_INPUT_A, a_str},
        {REDIS_KEY_INPUT_B, b_str},
        {REDIS_KEY_RESULT, gcd_str},
        {REDIS_KEY_ELAPSED, elapsed_ms_str},
        {REDIS_KEY_TASK_TYPE, get_task_type_name(task->base.task_type)},
        {REDIS_KEY_ALGORITHM, task->base.algorithm},
        {REDIS_KEY_STATUS, status_str}};

    int pair_count = sizeof(redis_pairs) / sizeof(redis_pairs[0]);

    if (xadd_redis_stream(redis_client, worker_config->stream_response,
                          redis_pairs, pair_count) != REDIS_SUCCESS) {
        LOG_ERROR("Fatal error: failed to execute XADD for ID %s.",
                  task->base.task_id);
        free(a_str);
        free(b_str);
        free(gcd_str);
        return EGRESS_ERR_CONN;
    }

    free(a_str);
    free(b_str);
    free(gcd_str);
    return EGRESS_SUCCESS;
}

static egress_status_e egress_send_lcm_result(worker_config_t* worker_config,
                                              redis_client_t* redis_client,
                                              lcm_task_t* task) {
    if (worker_config == NULL || redis_client == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to "
            "egress_send_gcd_result.");
        return EGRESS_ERR_ARGS;
    }

    if (worker_config->stream_response == NULL ||
        worker_config->stream_response[0] == '\0') {
        LOG_ERROR("worker_config->stream_response is NULL.");
        return EGRESS_ERR_ARGS;
    }

    if (task->base.task_id[0] == '\0') {
        LOG_ERROR("task->base.task_id is blank.");
        return EGRESS_ERR_ARGS;
    }

    char* a_str = mpz_get_str(NULL, 10, task->a);
    if (a_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for a_str");
        return EGRESS_ERR_MEMORY;
    }

    char* b_str = mpz_get_str(NULL, 10, task->b);
    if (b_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for b_str");
        return EGRESS_ERR_MEMORY;
    }

    char* lcm_str = mpz_get_str(NULL, 10, task->lcm);
    if (lcm_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for lcm_str");
        return EGRESS_ERR_MEMORY;
    }

    if (task->base.algorithm[0] == '\0') {
        LOG_WARN("Fatal error: task->base.algorithm is blank.");
    }

    char elapsed_ms_str[32];
    snprintf(elapsed_ms_str, sizeof(elapsed_ms_str), "%.3f",
             task->base.elapsed_time_ms);
    elapsed_ms_str[sizeof(elapsed_ms_str) - 1] = '\0';

    const char* status_str = get_status_name(task->base.status);
    if (status_str == NULL || strcmp(status_str, "UNKNOWN") == 0 ||
        strcmp(status_str, "UNHANDLED") == 0) {
        LOG_ERROR("Error: unknown status code. (%d)", task->base.status);
        status_str = "UNKNOWN";
    }

    redis_pair_t redis_pairs[] = {
        {REDIS_KEY_TASK_ID, task->base.task_id},
        {REDIS_KEY_INPUT_A, a_str},
        {REDIS_KEY_INPUT_B, b_str},
        {REDIS_KEY_RESULT, lcm_str},
        {REDIS_KEY_ELAPSED, elapsed_ms_str},
        {REDIS_KEY_TASK_TYPE, get_task_type_name(task->base.task_type)},
        {REDIS_KEY_ALGORITHM, task->base.algorithm},
        {REDIS_KEY_STATUS, status_str}};

    int pair_count = sizeof(redis_pairs) / sizeof(redis_pairs[0]);

    if (xadd_redis_stream(redis_client, worker_config->stream_response,
                          redis_pairs, pair_count) != REDIS_SUCCESS) {
        LOG_ERROR("Fatal error: failed to execute XADD for ID %s.",
                  task->base.task_id);
        free(a_str);
        free(b_str);
        free(lcm_str);
        return EGRESS_ERR_CONN;
    }

    free(a_str);
    free(b_str);
    free(lcm_str);
    return EGRESS_SUCCESS;
}

static egress_status_e egress_send_extended_euclidean_result(
    worker_config_t* worker_config,
    redis_client_t* redis_client,
    extended_euclidean_task_t* task) {
    if (worker_config == NULL || redis_client == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to "
            "egress_send_extended_euclidean_result.");
        return EGRESS_ERR_ARGS;
    }

    if (worker_config->stream_response == NULL ||
        worker_config->stream_response[0] == '\0') {
        LOG_ERROR("worker_config->stream_response is NULL.");
        return EGRESS_ERR_ARGS;
    }

    if (task->base.task_id[0] == '\0') {
        LOG_ERROR("task->base.task_id is blank.");
        return EGRESS_ERR_ARGS;
    }

    char* a_str = mpz_get_str(NULL, 10, task->a);
    if (a_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for a_str");
        return EGRESS_ERR_MEMORY;
    }

    char* b_str = mpz_get_str(NULL, 10, task->b);
    if (b_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for b_str");
        return EGRESS_ERR_MEMORY;
    }

    char* x_str = mpz_get_str(NULL, 10, task->x);
    if (x_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for x_str");
        return EGRESS_ERR_MEMORY;
    }

    char* y_str = mpz_get_str(NULL, 10, task->y);
    if (y_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for y_str");
        return EGRESS_ERR_MEMORY;
    }

    char* gcd_str = mpz_get_str(NULL, 10, task->gcd);
    if (gcd_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for gcd_str");
        return EGRESS_ERR_MEMORY;
    }

    if (task->base.algorithm[0] == '\0') {
        LOG_WARN("Fatal error: task->base.algorithm is blank.");
    }

    char elapsed_ms_str[32];
    snprintf(elapsed_ms_str, sizeof(elapsed_ms_str), "%.3f",
             task->base.elapsed_time_ms);
    elapsed_ms_str[sizeof(elapsed_ms_str) - 1] = '\0';

    const char* status_str = get_status_name(task->base.status);
    if (status_str == NULL || strcmp(status_str, "UNKNOWN") == 0 ||
        strcmp(status_str, "UNHANDLED") == 0) {
        LOG_ERROR("Error: unknown status code. (%d)", task->base.status);
        status_str = "UNKNOWN";
    }

    redis_pair_t redis_pairs[] = {
        {REDIS_KEY_TASK_ID, task->base.task_id},
        {REDIS_KEY_INPUT_A, a_str},
        {REDIS_KEY_INPUT_B, b_str},
        {REDIS_KEY_RESULT_X, x_str},
        {REDIS_KEY_RESULT_Y, y_str},
        {REDIS_KEY_RESULT, gcd_str},
        {REDIS_KEY_ELAPSED, elapsed_ms_str},
        {REDIS_KEY_TASK_TYPE, get_task_type_name(task->base.task_type)},
        {REDIS_KEY_ALGORITHM, task->base.algorithm},
        {REDIS_KEY_STATUS, status_str}};

    int pair_count = sizeof(redis_pairs) / sizeof(redis_pairs[0]);

    if (xadd_redis_stream(redis_client, worker_config->stream_response,
                          redis_pairs, pair_count) != REDIS_SUCCESS) {
        LOG_ERROR("Fatal error: failed to execute XADD for ID %s.",
                  task->base.task_id);
        free(a_str);
        free(b_str);
        free(x_str);
        free(y_str);
        free(gcd_str);
        return EGRESS_ERR_CONN;
    }

    free(a_str);
    free(b_str);
    free(x_str);
    free(y_str);
    free(gcd_str);
    return EGRESS_SUCCESS;
}

static egress_status_e egress_send_primality_test_result(
    worker_config_t* worker_config,
    redis_client_t* redis_client,
    primality_test_task_t* task) {
    if (worker_config == NULL || redis_client == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to "
            "egress_send_primality_test_result.");
        return EGRESS_ERR_ARGS;
    }

    if (worker_config->stream_response == NULL ||
        worker_config->stream_response[0] == '\0') {
        LOG_ERROR("worker_config->stream_response is NULL.");
        return EGRESS_ERR_ARGS;
    }

    if (task->base.task_id[0] == '\0') {
        LOG_ERROR("task->base.task_id is blank.");
        return EGRESS_ERR_ARGS;
    }

    char* input_str = mpz_get_str(NULL, 10, task->input);
    if (input_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for input_str");
        return EGRESS_ERR_MEMORY;
    }

    if (task->base.algorithm[0] == '\0') {
        LOG_WARN("Fatal error: task->base.algorithm is blank.");
    }

    char* result_str = task->is_prime ? "true" : "false";

    char elapsed_ms_str[32];
    snprintf(elapsed_ms_str, sizeof(elapsed_ms_str), "%.3f",
             task->base.elapsed_time_ms);
    elapsed_ms_str[sizeof(elapsed_ms_str) - 1] = '\0';

    const char* status_str = get_status_name(task->base.status);
    if (status_str == NULL || strcmp(status_str, "UNKNOWN") == 0 ||
        strcmp(status_str, "UNHANDLED") == 0) {
        LOG_ERROR("Error: unknown status code. (%d)", task->base.status);
        status_str = "UNKNOWN";
    }

    redis_pair_t redis_pairs[] = {
        {REDIS_KEY_TASK_ID, task->base.task_id},
        {REDIS_KEY_INPUT, input_str},
        {REDIS_KEY_RESULT, result_str},
        {REDIS_KEY_ELAPSED, elapsed_ms_str},
        {REDIS_KEY_TASK_TYPE, get_task_type_name(task->base.task_type)},
        {REDIS_KEY_ALGORITHM, task->base.algorithm},
        {REDIS_KEY_STATUS, status_str}};

    int pair_count = sizeof(redis_pairs) / sizeof(redis_pairs[0]);

    if (xadd_redis_stream(redis_client, worker_config->stream_response,
                          redis_pairs, pair_count) != REDIS_SUCCESS) {
        LOG_ERROR("Fatal error: failed to execute XADD for ID %s.",
                  task->base.task_id);
        free(input_str);
        return EGRESS_ERR_CONN;
    }

    free(input_str);
    return EGRESS_SUCCESS;
}

static egress_status_e array_to_str(mpz_t* arr, int arr_size, char** str_out) {
    if (str_out == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to array_to_str");
        return EGRESS_ERR_ARGS;
    }

    if (arr_size == 0 || arr == NULL) {
        LOG_WARN("Input array is empty.");
        *str_out = strdup("");
        return EGRESS_SUCCESS;
    }

    size_t required_size = arr_size * 80 + 1;
    char* buffer = (char*)malloc(required_size);

    if (buffer == NULL) {
        LOG_ERROR("Fatal Error: failed to allocate memory for buffer.");
        *str_out = NULL;
        return EGRESS_ERR_MEMORY;
    }

    size_t offset = 0;

    char* tmp = mpz_get_str(NULL, 10, arr[0]);
    offset += snprintf(buffer + offset, required_size - offset, "%s", tmp);
    free(tmp);

    for (int i = 1; i < arr_size; i++) {
        tmp = mpz_get_str(NULL, 10, arr[i]);
        offset += snprintf(buffer + offset, required_size - offset, "*%s", tmp);
        free(tmp);
    }

    *str_out = buffer;
    return EGRESS_SUCCESS;
}

static egress_status_e egress_send_factorize_result(
    worker_config_t* worker_config,
    redis_client_t* redis_client,
    factorize_task_t* task) {
    if (worker_config == NULL || redis_client == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to "
            "egress_send_factorize_result.");
        return EGRESS_ERR_ARGS;
    }

    if (worker_config->stream_response == NULL ||
        worker_config->stream_response[0] == '\0') {
        LOG_ERROR("Fatal error: worker_config->stream_response is NULL.");
        return EGRESS_ERR_ARGS;
    }

    if (task->base.task_id[0] == '\0') {
        LOG_ERROR("Fatal error: task->base.task_id is blank.");
        return EGRESS_ERR_ARGS;
    }

    if (task->factors == NULL && task->factor_count > 0) {
        LOG_ERROR("Fatal error: task->factors is NULL but count > 0.");
        return EGRESS_ERR_ARGS;
    }

    char* input_str = mpz_get_str(NULL, 10, task->input);
    if (input_str == NULL) {
        LOG_ERROR("Fatal error: failed to allocate memory for input_str");
        return EGRESS_ERR_MEMORY;
    }

    if (task->base.algorithm[0] == '\0') {
        LOG_WARN("Fatal error: task->base.algorithm is blank.");
    }

    char elapsed_ms_str[32];
    snprintf(elapsed_ms_str, sizeof(elapsed_ms_str), "%.3f",
             task->base.elapsed_time_ms);
    elapsed_ms_str[sizeof(elapsed_ms_str) - 1] = '\0';

    char* result_str = NULL;
    egress_status_e status =
        array_to_str(task->factors, task->factor_count, &result_str);

    if (status != EGRESS_SUCCESS) {
        LOG_ERROR("Fatal error: failed to serialize factors.");
        free(input_str);
        return status;
    }

    const char* status_str = get_status_name(task->base.status);

    if (status_str == NULL || strcmp(status_str, "UNKNOWN") == 0 ||
        strcmp(status_str, "UNHANDLED") == 0) {
        LOG_ERROR("Error: unknown status code. (%d)", task->base.status);
        status_str = "UNKNOWN";
    }

    redis_pair_t redis_pairs[] = {
        {REDIS_KEY_TASK_ID, task->base.task_id},
        {REDIS_KEY_INPUT, input_str},
        {REDIS_KEY_RESULT, result_str},
        {REDIS_KEY_ELAPSED, elapsed_ms_str},
        {REDIS_KEY_TASK_TYPE, get_task_type_name(task->base.task_type)},
        {REDIS_KEY_ALGORITHM, task->base.algorithm},
        {REDIS_KEY_STATUS, status_str}};

    int pair_count = sizeof(redis_pairs) / sizeof(redis_pairs[0]);

    if (xadd_redis_stream(redis_client, worker_config->stream_response,
                          redis_pairs, pair_count) != REDIS_SUCCESS) {
        LOG_ERROR("Fatal error: failed to execute XADD for ID %s.",
                  task->base.task_id);
        free(input_str);
        free(result_str);
        return EGRESS_ERR_CONN;
    }

    free(input_str);
    free(result_str);
    return EGRESS_SUCCESS;
}

egress_status_e egress_send_status(worker_config_t* worker_config,
                                   redis_client_t* redis_client,
                                   base_task_t* task) {
    if (worker_config == NULL || redis_client == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to egress_send_status.");
        return EGRESS_ERR_ARGS;
    }

    if (worker_config->stream_response == NULL ||
        worker_config->stream_response[0] == '\0') {
        LOG_ERROR("Fatal error: worker_config->stream_response is NULL.");
        return EGRESS_ERR_ARGS;
    }

    if (task->task_id[0] == '\0') {
        LOG_ERROR("Fatal error: task->task_id is blank.");
        return EGRESS_ERR_ARGS;
    }

    const char* status_str = get_status_name(task->status);
    if (status_str == NULL) {
        LOG_ERROR("Error: unknown status code. (%d)", task->status);
        status_str = "UNKNOWN";
    }

    redis_pair_t redis_pairs[] = {
        {REDIS_KEY_TASK_ID, task->task_id},
        {REDIS_KEY_TASK_TYPE, get_task_type_name(task->task_type)},
        {REDIS_KEY_STATUS, status_str}};

    int pair_count = sizeof(redis_pairs) / sizeof(redis_pairs[0]);

    if (xadd_redis_stream(redis_client, worker_config->stream_response,
                          redis_pairs, pair_count) != REDIS_SUCCESS) {
        LOG_ERROR(
            "Fatal error: failed to execute XADD to stream %s for Task ID "
            "%s.",
            worker_config->stream_response, task->task_id);
        return EGRESS_ERR_CONN;
    }

    return EGRESS_SUCCESS;
}

egress_status_e egress_send_result(worker_config_t* worker_config,
                                   redis_client_t* redis_client,
                                   base_task_t* task) {
    if (worker_config == NULL || redis_client == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to egress_send_result.");
        return EGRESS_ERR_ARGS;
    }

    switch (task->task_type) {
        case GCD:
            return egress_send_gcd_result(worker_config, redis_client,
                                          (gcd_task_t*)task);
        case LCM:
            return egress_send_lcm_result(worker_config, redis_client,
                                          (lcm_task_t*)task);
        case EXTENDED_GCD:
            return egress_send_extended_euclidean_result(
                worker_config, redis_client, (extended_euclidean_task_t*)task);
        case PRIMALITY_TEST:
            return egress_send_primality_test_result(
                worker_config, redis_client, (primality_test_task_t*)task);
        case FACTORIZE:
            return egress_send_factorize_result(worker_config, redis_client,
                                                (factorize_task_t*)task);
        case UNKNOWN_TASK:
            LOG_ERROR("'%s' type received. Failed to egress task result.",
                      get_task_type_name(UNKNOWN_TASK));
            return EGRESS_ERR_ARGS;
        default:
            LOG_ERROR("Argument error: not valid task type: %d",
                      task->task_type);
            return EGRESS_ERR_ARGS;
    }
}

egress_status_e egress_send_xack(worker_config_t* worker_config,
                                 redis_client_t* redis_client,
                                 base_task_t* task) {
    if (worker_config == NULL || redis_client == NULL || task == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer is passed to egress_send_xack.");
        return EGRESS_ERR_ARGS;
    }

    if (worker_config->stream_request == NULL ||
        worker_config->stream_request[0] == '\0') {
        LOG_ERROR("Fatal error: worker_config->stream_request is NULL.");
        return EGRESS_ERR_ARGS;
    }

    if (worker_config->consumer_group == NULL ||
        worker_config->consumer_group[0] == '\0') {
        LOG_ERROR("Fatal error: worker_config->consumer_group is NULL.");
        return EGRESS_ERR_ARGS;
    }

    if (task->stream_msg_id[0] == '\0') {
        LOG_ERROR("Fatal error: task->stream_msg_id is blank.");
        return EGRESS_ERR_ARGS;
    }

    if (xack_redis_stream(redis_client, worker_config->stream_request,
                          worker_config->consumer_group,
                          task->stream_msg_id) != REDIS_SUCCESS) {
        return EGRESS_ERR_CONN;
    }

    return EGRESS_SUCCESS;
}