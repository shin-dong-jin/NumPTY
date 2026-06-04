#include "calculator/number_theory/eratosthenes_sieve.h"

#include "calculator/calculator.h"
#include "common.h"
#include "logger.h"

int* g_small_primes = NULL;
int g_small_prime_count = 0;

calculate_status_e initialize_sieve() {
    char* is_composite = calloc(TRIAL_DIVISION_LIMIT + 1, 1);
    if (is_composite == NULL) {
        LOG_ERROR("Fatal error: cannot allocate memory for composite array.");
        return CALCULATE_ERR_MEMORY;
    }

    for (int i = 2; (long long)i * i <= TRIAL_DIVISION_LIMIT; i++) {
        if (!is_composite[i]) {
            for (int j = i * i; j <= TRIAL_DIVISION_LIMIT; j += i) {
                is_composite[j] = 1;
            }
        }
    }

    g_small_primes = malloc(sizeof(int) * 100000);
    if (g_small_primes == NULL) {
        LOG_ERROR("Fatal error: cannot allocate memory for primes array.");
        free(is_composite);
        return CALCULATE_ERR_MEMORY;
    }

    g_small_prime_count = 0;
    for (int i = 2; i <= TRIAL_DIVISION_LIMIT; i++) {
        if (!is_composite[i]) {
            g_small_primes[g_small_prime_count++] = i;
        }
    }
    free(is_composite);

    LOG_INFO("number_theory_init: %d primes up to %d", g_small_prime_count,
             TRIAL_DIVISION_LIMIT);

    return CALCULATE_SUCCESS;
}

void cleanup_sieve() {
    free(g_small_primes);
    g_small_primes = NULL;
}