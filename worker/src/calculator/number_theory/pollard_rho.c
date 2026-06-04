#include "calculator/number_theory/pollard_rho.h"

#include "calculator/calculator.h"
#include "calculator/gmp_random.h"
#include "calculator/number_theory/euclidean.h"
#include "common.h"
#include "logger.h"

calculate_status_e pollard_rho(mpz_t factor,
                               const mpz_t n,
                               unsigned long max_steps) {
    if (mpz_even_p(n)) {
        mpz_set_ui(factor, 2);
        return CALCULATE_SUCCESS;
    }

    unsigned long total_steps = 0;
    calculate_status_e result = CALCULATE_NOT_FOUND;

    mpz_t x, y, c, q, r, ys, d, temp;
    mpz_inits(x, y, c, q, r, ys, d, temp, NULL);

    while (total_steps < max_steps) {
        mpz_urandomm(y, g_rstate, n);
        mpz_urandomm(c, g_rstate, n);

        if (mpz_cmp_ui(c, 0) == 0 || mpz_cmp_ui(c, 2) == 0) {
            continue;
        }

        mpz_set_ui(q, 1);
        mpz_set_ui(r, 1);
        mpz_set_ui(d, 1);

        do {
            mpz_set(x, y);

            for (unsigned long i = 0; i < mpz_get_ui(r); i++) {
                mpz_mul(y, y, y);
                mpz_add(y, y, c);
                mpz_mod(y, y, n);

                total_steps++;
            }

            unsigned long k = 0;
            while (k < mpz_get_ui(r) && mpz_cmp_ui(d, 1) == 0) {
                if (total_steps >= max_steps) {
                    goto cleanup;
                }

                mpz_set(ys, y);

                for (int i = 0; i < BATCH_GCD && i < (int)(mpz_get_ui(r) - k);
                     i++) {
                    mpz_mul(y, y, y);
                    mpz_add(y, y, c);
                    mpz_mod(y, y, n);

                    mpz_sub(temp, x, y);
                    mpz_abs(temp, temp);
                    mpz_mul(q, q, temp);
                    mpz_mod(q, q, n);

                    total_steps++;
                }

                gcd(d, q, n);
                k += BATCH_GCD;
            }

            mpz_mul_ui(r, r, 2);

        } while (mpz_cmp_ui(d, 1) == 0);

        if (mpz_cmp(d, n) == 0) {
            do {
                mpz_mul(ys, ys, ys);
                mpz_add(ys, ys, c);
                mpz_mod(ys, ys, n);

                mpz_sub(temp, x, ys);
                mpz_abs(temp, temp);
                gcd(d, temp, n);

                total_steps++;
                if (total_steps >= max_steps) {
                    goto cleanup;
                }
            } while (mpz_cmp_ui(d, 1) == 0);
        }

        if (mpz_cmp(d, n) != 0) {
            mpz_set(factor, d);
            result = CALCULATE_SUCCESS;
            break;
        }
    }

cleanup:
    mpz_clears(x, y, c, q, r, ys, d, temp, NULL);
    return result;
}