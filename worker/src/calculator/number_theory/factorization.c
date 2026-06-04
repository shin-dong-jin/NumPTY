#include "calculator/number_theory/factorization.h"

#include "calculator/calculator.h"
#include "calculator/number_theory/ecm.h"
#include "calculator/number_theory/miller_rabin.h"
#include "calculator/number_theory/pollard_rho.h"
#include "calculator/number_theory/trial_division.h"
#include "common.h"
#include "logger.h"

static calculate_status_e factorize_recursive(const mpz_t n,
                                              mpz_t* temp,
                                              int* index) {
    if (mpz_cmp_ui(n, 1) == 0) {
        return CALCULATE_SUCCESS;
    }

    if (primality_test_miller_rabin(n, 20)) {
        mpz_init(temp[*index]);
        mpz_set(temp[*index], n);
        (*index)++;
        return CALCULATE_SUCCESS;
    }

    mpz_t factor, quotient;
    mpz_inits(factor, quotient, NULL);

    calculate_status_e calculate_status =
        pollard_rho(factor, n, POLLARD_RHO_MAX_STEPS);

    if (calculate_status != CALCULATE_SUCCESS) {
        LOG_DEBUG("Pollard-Rho failed, falling back to ECM");

        calculate_status = ecm(factor, n);

        if (calculate_status != CALCULATE_SUCCESS) {
            LOG_ERROR("ECM failed: n with %zu digits", mpz_sizeinbase(n, 10));

            mpz_init(temp[*index]);
            mpz_set(temp[*index], n);
            (*index)++;
            mpz_clears(factor, quotient, NULL);
            return CALCULATE_NOT_FOUND;
        }
    }

    mpz_divexact(quotient, n, factor);
    calculate_status = factorize_recursive(factor, temp, index);

    if (calculate_status != CALCULATE_SUCCESS) {
        mpz_clears(factor, quotient, NULL);
        return calculate_status;
    }

    calculate_status = factorize_recursive(quotient, temp, index);

    mpz_clears(factor, quotient, NULL);
    return calculate_status;
}

calculate_status_e factorize(const mpz_t n, mpz_t* temp, int* index) {
    if (mpz_cmp_ui(n, 1) == 0) {
        LOG_ERROR("Invalid input: n must not be less than 2");
        return CALCULATE_ERR_INPUT;
    }

    mpz_t remaining;
    mpz_init(remaining);

    trial_division(n, remaining, temp, index);

    if (primality_test_miller_rabin(remaining, 20)) {
        mpz_init(temp[*index]);
        mpz_set(temp[*index], remaining);
        (*index)++;
        mpz_clear(remaining);
        return CALCULATE_SUCCESS;
    }

    calculate_status_e calculate_status =
        factorize_recursive(remaining, temp, index);
    mpz_clear(remaining);

    if (calculate_status != CALCULATE_SUCCESS) {
        LOG_ERROR("Failed to factorize_recursive.");
    }

    return calculate_status;
}