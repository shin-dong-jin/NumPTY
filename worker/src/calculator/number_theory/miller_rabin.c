#include "calculator/number_theory/miller_rabin.h"

#include "calculator/gmp_random.h"
#include "common.h"
#include "logger.h"

static int miller_rabin_round(const mpz_t n, const mpz_t a, const mpz_t d, unsigned long r) {
    mpz_t x, n_1;
    mpz_inits(x, n_1, NULL);

    mpz_sub_ui(n_1, n, 1);
    mpz_powm(x, a, d, n);

    if (mpz_cmp_ui(x, 1) == 0 || mpz_cmp(x, n_1) == 0) {
        mpz_clears(x, n_1, NULL);
        return 1;
    }

    for (unsigned long i = 0; i < r - 1; i++) {
        mpz_powm_ui(x, x, 2, n);

        if (mpz_cmp(x, n_1) == 0) {
            mpz_clears(x, n_1, NULL);
            return 1;
        }
    }

    mpz_clears(x, n_1, NULL);
    return 0;
}

int primality_test_miller_rabin(const mpz_t n, int k) {
    if (mpz_cmp_ui(n, 2) < 0) {
        return 0;
    }

    if (mpz_cmp_ui(n, 2) == 0) {
        return 1;
    }

    if (mpz_cmp_ui(n, 3) == 0) {
        return 1;
    }

    if (mpz_even_p(n)) {
        return 0;
    }

    mpz_t d, n_1, a;
    mpz_inits(d, n_1, a, NULL);

    mpz_sub_ui(n_1, n, 1);
    mpz_set(d, n_1);

    unsigned long r = 0;
    while (mpz_even_p(d)) {
        mpz_divexact_ui(d, d, 2);
        r++;
    }

    int result = 1;
    for (int i = 0; i < k; i++) {
        mpz_sub_ui(a, n_1, 2);
        mpz_urandomm(a, g_rstate, a);
        mpz_add_ui(a, a, 2);

        if (!miller_rabin_round(n, a, d, r)) {
            result = 0;
            break;
        }
    }

    mpz_clears(d, n_1, a, NULL);
    return result;
}