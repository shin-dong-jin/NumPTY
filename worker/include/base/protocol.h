#pragma once

#define PROTOCOL_DELIMITER ":"
#define REDIS_KEY_TASK_ID "_id"
#define REDIS_KEY_TASK_TYPE "type"
#define REDIS_KEY_INPUT "target"
#define REDIS_KEY_INPUT_A "targetA"
#define REDIS_KEY_INPUT_B "targetB"
#define REDIS_KEY_RESULT "result"
#define REDIS_KEY_RESULT_X "resultX"
#define REDIS_KEY_RESULT_Y "resultY"
#define REDIS_KEY_ELAPSED "elapsedMs"
#define REDIS_KEY_STATUS "status"
#define REDIS_KEY_ALGORITHM "algorithms"

#include <gmp.h>

enum calculate_status_e;
struct base_task_t;

typedef enum calculate_status_e (*task_exec_fn)(struct base_task_t*);

typedef enum task_status_e {
    PENDING = 0,
    WORKING = 1,
    COMPLETED = 2,
    FAILED = 3,
    UNKNOWN = 99
} task_status_e;

typedef enum task_type_e {
    GCD = 0,
    LCM = 1,
    EXTENDED_GCD = 2,
    PRIMALITY_TEST = 3,
    FACTORIZE = 4,
    UNKNOWN_TASK = 99
} task_type_e;

typedef struct base_task_t {
    char stream_msg_id[32];
    char task_id[32];
    task_type_e task_type;
    char algorithm[64];
    double elapsed_time_ms;
    task_status_e status;
} base_task_t;

typedef struct gcd_task_t {
    base_task_t base;
    mpz_t a;
    mpz_t b;
    mpz_t gcd;
} gcd_task_t;

typedef struct lcm_task_t {
    base_task_t base;
    mpz_t a;
    mpz_t b;
    mpz_t lcm;
} lcm_task_t;

typedef struct extended_euclidean_task_t {
    base_task_t base;
    mpz_t a;
    mpz_t b;
    mpz_t x;
    mpz_t y;
    mpz_t gcd;
} extended_euclidean_task_t;

typedef struct primality_test_task_t {
    base_task_t base;
    mpz_t input;
    int is_prime;
} primality_test_task_t;

typedef struct factorize_task_t {
    base_task_t base;
    mpz_t input;
    mpz_t* factors;
    int factor_count;
} factorize_task_t;

const char* get_status_name(task_status_e task_status);
const char* get_task_type_name(task_type_e task_type);
const char* get_task_algorithm_name(task_type_e task_type);
base_task_t* create_task(task_type_e task_type);
void free_task(base_task_t** task);