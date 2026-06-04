#include "calculator/number_theory/trial_division.h"

#include "calculator/number_theory/eratosthenes_sieve.h"
#include "common.h"
#include "logger.h"

void trial_division(const mpz_t n, mpz_t remaining, mpz_t* temp, int* index) {
    if (g_small_primes == NULL || g_small_prime_count == 0) {
        LOG_ERROR("Fatal error: sieve is not initialized.");
        mpz_set(remaining, n);
        return;
    }

    mpz_set(remaining, n);

    mpz_t sqrt_n;
    mpz_init(sqrt_n);
    mpz_sqrt(sqrt_n, remaining);

    for (int i = 0; i < g_small_prime_count; i++) {
        unsigned long p = g_small_primes[i];

        if (mpz_cmp_ui(sqrt_n, p) < 0) {
            break;
        }

        while (mpz_divisible_ui_p(remaining, p)) {
            mpz_init(temp[*index]);
            mpz_set_ui(temp[*index], p);
            (*index)++;
            mpz_divexact_ui(remaining, remaining, p);

            mpz_sqrt(sqrt_n, remaining);
        }
    }

    mpz_clear(sqrt_n);
}