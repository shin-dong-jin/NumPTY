#include "calculator/number_theory/exec_number_theory.h"

#include "base/protocol.h"
#include "calculator/calculator.h"
#include "calculator/number_theory/euclidean.h"
#include "calculator/number_theory/factorization.h"
#include "calculator/number_theory/miller_rabin.h"
#include "calculator/sorting/sorting.h"
#include "common.h"
#include "logger.h"

calculate_status_e exec_gcd(base_task_t* base) {
    if (base == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to exec_gcd.");
        return CALCULATE_ERR_ARGS;
    }

    if (base->task_type != GCD) {
        LOG_ERROR("Fatal error: invalid task type (expected: %s, actual: %s)",
                  get_task_type_name(GCD), get_task_type_name(base->task_type));
        return CALCULATE_ERR_ARGS;
    }

    gcd_task_t* task = (gcd_task_t*)base;

    if (mpz_cmp_ui(task->a, 0) == 0 || mpz_cmp_ui(task->b, 0) == 0) {
        LOG_ERROR("Invalid input: inputs cannot be zero.");
        return CALCULATE_ERR_INPUT;
    }

    gcd_recursive(task->gcd, task->a, task->b);

    return CALCULATE_SUCCESS;
}

calculate_status_e exec_lcm(base_task_t* base) {
    if (base == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to exec_lcm.");
        return CALCULATE_ERR_ARGS;
    }

    if (base->task_type != LCM) {
        LOG_ERROR("Fatal error: invalid task type (expected: %s, actual: %s)",
                  get_task_type_name(LCM), get_task_type_name(base->task_type));
        return CALCULATE_ERR_ARGS;
    }

    lcm_task_t* task = (lcm_task_t*)base;

    if (mpz_cmp_ui(task->a, 0) == 0 || mpz_cmp_ui(task->b, 0) == 0) {
        LOG_ERROR("Invalid input: inputs cannot be zero.");
        return CALCULATE_ERR_INPUT;
    }

    lcm(task->lcm, task->a, task->b);

    return CALCULATE_SUCCESS;
}

calculate_status_e exec_extended_euclidean(base_task_t* base) {
    if (base == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to exec_extended_gcd.");
        return CALCULATE_ERR_ARGS;
    }

    if (base->task_type != EXTENDED_GCD) {
        LOG_ERROR("Fatal error: invalid task type (expected: %s, actual: %s)",
                  get_task_type_name(EXTENDED_GCD), get_task_type_name(base->task_type));
        return CALCULATE_ERR_ARGS;
    }

    extended_euclidean_task_t* task = (extended_euclidean_task_t*)base;

    if (mpz_cmp_ui(task->a, 0) == 0 || mpz_cmp_ui(task->b, 0) == 0) {
        LOG_ERROR("Invalid input: inputs cannot be zero.");
        return CALCULATE_ERR_INPUT;
    }

    extended_euclidean(task->gcd, task->x, task->y, task->a, task->b);

    return CALCULATE_SUCCESS;
}

calculate_status_e exec_primality_test_miller_rabin(base_task_t* base) {
    if (base == NULL) {
        LOG_ERROR(
            "Invalid arguments: NULL pointer passed to "
            "exec_primality_test_miller_rabin.");
        return CALCULATE_ERR_ARGS;
    }

    if (base->task_type != PRIMALITY_TEST) {
        LOG_ERROR("Fatal error: invalid task type(expected: %s, actual: %s)",
                  get_task_type_name(PRIMALITY_TEST), get_task_type_name(base->task_type));
        return CALCULATE_ERR_ARGS;
    }

    primality_test_task_t* task = (primality_test_task_t*)base;

    if (mpz_cmp_ui(task->input, 1) < 0) {
        LOG_ERROR("Invalid input: input must be positive integer.");
        return CALCULATE_ERR_INPUT;
    }

    task->is_prime = primality_test_miller_rabin(task->input, 30);

    return CALCULATE_SUCCESS;
}

calculate_status_e exec_factorize(base_task_t* base) {
    if (base == NULL) {
        LOG_ERROR("Invalid arguments: NULL pointer passed to exec_factorize.");
        return CALCULATE_ERR_ARGS;
    }

    if (base->task_type != FACTORIZE) {
        LOG_ERROR("Fatal error: invalid task type (expected: %s, actual: %s)",
                  get_task_type_name(FACTORIZE), get_task_type_name(base->task_type));
        return CALCULATE_ERR_ARGS;
    }

    factorize_task_t* task = (factorize_task_t*)base;

    if (mpz_cmp_ui(task->input, 2) < 0) {
        LOG_ERROR("Invalid input: input must not be less than 2");
        return CALCULATE_ERR_INPUT;
    }

    mpz_t temp[MAX_FACTORS];
    int count = 0;

    calculate_status_e calculate_status = factorize(task->input, temp, &count);

    if (calculate_status != CALCULATE_SUCCESS) {
        LOG_ERROR("Failed to factorize.");
        for (int i = 0; i < count; i++) {
            mpz_clear(temp[i]);
        }
        return calculate_status;
    }

    if (count == 0) {
        LOG_WARN("Unexpected Error: count is 0 after factorize.");
        task->factors = NULL;
        task->factor_count = 0;
        return CALCULATE_ERR_INPUT;
    }

    task->factor_count = count;
    task->factors = (mpz_t*)malloc(sizeof(mpz_t) * count);

    if (task->factors == NULL) {
        LOG_ERROR("Fatal error: cannot allocate memory for task->factors array.");

        for (int i = 0; i < count; i++) {
            mpz_clear(temp[i]);
        }

        return CALCULATE_ERR_MEMORY;
    }

    for (int i = 0; i < count; i++) {
        mpz_init_set(task->factors[i], temp[i]);
        mpz_clear(temp[i]);
    }

    quicksort_mpz(task->factors, 0, task->factor_count - 1);
    return calculate_status;
}