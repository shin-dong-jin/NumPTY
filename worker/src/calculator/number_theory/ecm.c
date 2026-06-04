#include "calculator/number_theory/ecm.h"

#include "calculator/calculator.h"
#include "calculator/gmp_random.h"
#include "calculator/number_theory/euclidean.h"
#include "common.h"
#include "logger.h"

typedef struct {
    mpz_t x;
    mpz_t z;
} mont_point_t;

static void mont_point_init(mont_point_t* P) {
    mpz_inits(P->x, P->z, NULL);
}

static void mont_point_clear(mont_point_t* P) {
    mpz_clears(P->x, P->z, NULL);
}

static void mont_point_set(mont_point_t* dest, const mont_point_t* src) {
    mpz_set(dest->x, src->x);
    mpz_set(dest->z, src->z);
}

static void mont_point_add(mont_point_t* R,
                           const mont_point_t* P,
                           const mont_point_t* Q,
                           const mont_point_t* diff,
                           const mpz_t n) {
    mpz_t u, v, rx, rz, temp;
    mpz_inits(u, v, rx, rz, temp, NULL);

    mpz_sub(u, P->x, P->z);
    mpz_add(temp, Q->x, Q->z);
    mpz_mul(u, u, temp);
    mpz_mod(u, u, n);

    mpz_add(v, P->x, P->z);
    mpz_sub(temp, Q->x, Q->z);
    mpz_mul(v, v, temp);
    mpz_mod(v, v, n);

    mpz_add(rx, u, v);
    mpz_mul(rx, rx, rx);
    mpz_mul(rx, diff->z, rx);
    mpz_mod(rx, rx, n);

    mpz_sub(rz, u, v);
    mpz_mul(rz, rz, rz);
    mpz_mul(rz, diff->x, rz);
    mpz_mod(rz, rz, n);

    mpz_set(R->x, rx);
    mpz_set(R->z, rz);

    mpz_clears(u, v, rx, rz, temp, NULL);
}

static void mont_point_double(mont_point_t* R,
                              const mont_point_t* P,
                              const mpz_t A24,
                              const mpz_t n) {
    mpz_t a, b, c, rx, rz;
    mpz_inits(a, b, c, rx, rz, NULL);

    mpz_add(a, P->x, P->z);
    mpz_mul(a, a, a);
    mpz_mod(a, a, n);

    mpz_sub(b, P->x, P->z);
    mpz_mul(b, b, b);
    mpz_mod(b, b, n);

    mpz_sub(c, a, b);
    mpz_mod(c, c, n);

    mpz_mul(rx, a, b);
    mpz_mod(rx, rx, n);

    mpz_mul(rz, A24, c);
    mpz_add(rz, rz, b);
    mpz_mul(rz, rz, c);
    mpz_mod(rz, rz, n);

    mpz_set(R->x, rx);
    mpz_set(R->z, rz);

    mpz_clears(a, b, c, rx, rz, NULL);
}

static void mont_point_scalar_mul(mont_point_t* R,
                                  const mpz_t k,
                                  const mont_point_t* P,
                                  const mpz_t A24,
                                  const mpz_t n) {
    mont_point_t R0, R1;
    mont_point_init(&R0);
    mont_point_init(&R1);

    mont_point_set(&R0, P);
    mont_point_double(&R1, P, A24, n);

    int bits = (int)mpz_sizeinbase(k, 2);

    for (int i = bits - 2; i >= 0; i--) {
        if (mpz_tstbit(k, i)) {
            mont_point_add(&R0, &R0, &R1, P, n);
            mont_point_double(&R1, &R1, A24, n);
        } else {
            mont_point_add(&R1, &R0, &R1, P, n);
            mont_point_double(&R0, &R0, A24, n);
        }
    }

    mont_point_set(R, &R0);
    mont_point_clear(&R0);
    mont_point_clear(&R1);
}

static int suyama_param(mpz_t A24,
                        mont_point_t* P,
                        const mpz_t sigma,
                        const mpz_t n) {
    mpz_t u, v, u3, v3, numerator, denominator, inverse, temp;
    mpz_inits(u, v, u3, v3, numerator, denominator, inverse, temp, NULL);

    mpz_mul(u, sigma, sigma);
    mpz_sub_ui(u, u, 5);
    mpz_mod(u, u, n);

    mpz_mul_ui(v, sigma, 4);
    mpz_mod(v, v, n);

    mpz_powm_ui(u3, u, 3, n);
    mpz_powm_ui(v3, v, 3, n);

    mpz_set(P->x, u3);
    mpz_set(P->z, v3);

    mpz_sub(temp, v, u);
    mpz_mod(temp, temp, n);
    mpz_powm_ui(temp, temp, 3, n);

    mpz_mul_ui(numerator, u, 3);
    mpz_add(numerator, numerator, v);
    mpz_mod(numerator, numerator, n);
    mpz_mul(numerator, numerator, temp);
    mpz_mod(numerator, numerator, n);

    mpz_mul_ui(denominator, u3, 16);
    mpz_mul(denominator, denominator, v);
    mpz_mod(denominator, denominator, n);

    if (!modulo_inverse(inverse, denominator, n)) {
        mpz_clears(u, v, u3, v3, numerator, denominator, inverse, temp, NULL);
        return -1;
    }

    mpz_mul(A24, numerator, inverse);
    mpz_mod(A24, A24, n);

    mpz_clears(u, v, u3, v3, numerator, denominator, inverse, temp, NULL);
    return 0;
}

static int* sieve_primes(int limit, int* count) {
    char* is_composite = calloc(limit + 1, 1);
    for (int i = 2; (long long)i * i <= limit; i++) {
        if (!is_composite[i]) {
            for (int j = i * i; j <= limit; j += i)
                is_composite[j] = 1;
        }
    }
    int* primes = malloc(sizeof(int) * (limit / 2 + 10));
    *count = 0;
    for (int i = 2; i <= limit; i++) {
        if (!is_composite[i])
            primes[(*count)++] = i;
    }
    free(is_composite);
    return primes;
}

static void choose_bounds(const mpz_t n,
                          unsigned long* b1,
                          unsigned long* b2,
                          int* max_curves) {
    size_t digits = mpz_sizeinbase(n, 10);

    if (digits < 10) {
        *b1 = 1000;
        *b2 = 100000;
        *max_curves = 10;
    } else if (digits < 15) {
        *b1 = 5000;
        *b2 = 500000;
        *max_curves = 50;
    } else if (digits < 20) {
        *b1 = 10000;
        *b2 = 1000000;
        *max_curves = 100;
    } else if (digits < 25) {
        *b1 = 50000;
        *b2 = 5000000;
        *max_curves = 200;
    } else if (digits < 30) {
        *b1 = 250000;
        *b2 = 25000000;
        *max_curves = 400;
    } else if (digits < 35) {
        *b1 = 1000000;
        *b2 = 100000000;
        *max_curves = 800;
    } else {
        *b1 = 3000000;
        *b2 = 300000000;
        *max_curves = 1000;
    }
}

static int ecm_stage1(mpz_t factor,
                      const mpz_t n,
                      mont_point_t* Q,
                      const mpz_t A24,
                      int* primes,
                      int prime_count,
                      unsigned long B1) {
    mpz_t g;
    mpz_init(g);

    for (int i = 0; i < prime_count; i++) {
        long long p = primes[i];
        long long pe = p;

        while (pe <= (long long)B1 / p) {
            pe *= p;
        }

        mpz_t k;
        mpz_init(k);
        mpz_set_ui(k, (unsigned long)pe);
        mont_point_scalar_mul(Q, k, Q, A24, n);
        mpz_clear(k);

        gcd(g, Q->z, n);
        if (mpz_cmp_ui(g, 1) > 0 && mpz_cmp(g, n) < 0) {
            mpz_set(factor, g);
            mpz_clear(g);
            return 1;
        }
        if (mpz_cmp(g, n) == 0) {
            mpz_clear(g);
            return -1;
        }
    }

    mpz_clear(g);
    return 0;
}

static int ecm_stage2(mpz_t factor,
                      const mpz_t n,
                      mont_point_t* Q,
                      const mpz_t A24,
                      const char* composite,
                      unsigned long B1,
                      unsigned long B2) {
    int rs[ECM_D];
    int r_count = 0;
    for (int r = 1; r <= ECM_D / 2; r++) {
        int g = r, d = ECM_D;

        while (d) {
            int t = d;
            d = g % d;
            g = t;
        }

        if (g == 1) {
            rs[r_count++] = r;
        }
    }

    mpz_t* baby_x = malloc(sizeof(mpz_t) * r_count);
    for (int i = 0; i < r_count; i++) {
        mpz_init(baby_x[i]);
    }

    mpz_t inv, g;
    mpz_inits(inv, g, NULL);

    for (int i = 0; i < r_count; i++) {
        mont_point_t rQ;
        mont_point_init(&rQ);
        mont_point_set(&rQ, Q);

        mpz_t k;
        mpz_init(k);
        mpz_set_ui(k, rs[i]);
        mont_point_scalar_mul(&rQ, k, Q, A24, n);
        mpz_clear(k);

        if (!modulo_inverse(inv, rQ.z, n)) {
            gcd(g, rQ.z, n);
            if (mpz_cmp_ui(g, 1) > 0 && mpz_cmp(g, n) < 0) {
                mpz_set(factor, g);
                mont_point_clear(&rQ);
                for (int j = 0; j < r_count; j++) {
                    mpz_clear(baby_x[j]);
                }
                free(baby_x);
                mpz_clears(inv, g, NULL);
                return 1;
            }
        } else {
            mpz_mul(baby_x[i], rQ.x, inv);
            mpz_mod(baby_x[i], baby_x[i], n);
        }

        mont_point_clear(&rQ);
    }

    int m0 = (int)(B1 / ECM_D);
    int m_max = (int)((B2 + ECM_D - 1) / ECM_D);

    mont_point_t G, G_prev, Q_D;
    mont_point_init(&G);
    mont_point_init(&G_prev);
    mont_point_init(&Q_D);

    {
        mpz_t k_prev;
        mpz_init_set_ui(k_prev, (unsigned long)((m0 - 1) * ECM_D));
        mont_point_scalar_mul(&G_prev, k_prev, Q, A24, n);
        mpz_clear(k_prev);
    }
    {
        mpz_t k0;
        mpz_init_set_ui(k0, (unsigned long)(m0 * ECM_D));
        mont_point_scalar_mul(&G, k0, Q, A24, n);
        mpz_clear(k0);
    }
    {
        mpz_t kD;
        mpz_init_set_ui(kD, ECM_D);
        mont_point_scalar_mul(&Q_D, kD, Q, A24, n);
        mpz_clear(kD);
    }

    mpz_t acc, diff, gx;
    mpz_inits(acc, diff, gx, NULL);
    mpz_set_ui(acc, 1);

    int found = 0;

    for (int m = m0 + 1; m <= m_max && !found; m++) {
        mont_point_t G_next;
        mont_point_init(&G_next);
        mont_point_add(&G_next, &G, &Q_D, &G_prev, n);

        mont_point_set(&G_prev, &G);  // G_prev = 현재 G
        mont_point_set(&G, &G_next);  // G = 다음 G
        mont_point_clear(&G_next);

        if (!modulo_inverse(inv, G.z, n)) {
            gcd(g, G.z, n);
            if (mpz_cmp_ui(g, 1) > 0 && mpz_cmp(g, n) < 0) {
                mpz_set(factor, g);
                found = 1;

                mpz_clears(acc, diff, gx, inv, g, NULL);
                mont_point_clear(&G);
                mont_point_clear(&G_prev);
                mont_point_clear(&Q_D);
                for (int i = 0; i < r_count; i++) {
                    mpz_clear(baby_x[i]);
                }
                free(baby_x);
                return found;
            }
            continue;
        }

        mpz_mul(gx, G.x, inv);
        mpz_mod(gx, gx, n);

        for (int i = 0; i < r_count; i++) {
            long long q1 = (long long)m * ECM_D + rs[i];
            long long q2 = (long long)m * ECM_D - rs[i];

            int valid =
                (q1 > (long long)B1 && q1 <= (long long)B2 && !composite[q1]) ||
                (q2 > (long long)B1 && q2 <= (long long)B2 && !composite[q2]);

            if (valid) {
                mpz_sub(diff, gx, baby_x[i]);
                mpz_mod(diff, diff, n);
                mpz_mul(acc, acc, diff);
                mpz_mod(acc, acc, n);
            }
        }

        if (m % 128 == 0) {
            gcd(factor, acc, n);
            if (mpz_cmp_ui(factor, 1) > 0 && mpz_cmp(factor, n) < 0) {
                found = 1;
            }
        }
    }

    if (!found) {
        gcd(factor, acc, n);
        found = (mpz_cmp_ui(factor, 1) > 0 && mpz_cmp(factor, n) < 0);
    }

    mpz_clears(acc, diff, gx, inv, g, NULL);
    mont_point_clear(&G);
    mont_point_clear(&G_prev);
    mont_point_clear(&Q_D);
    for (int i = 0; i < r_count; i++) {
        mpz_clear(baby_x[i]);
    }
    free(baby_x);
    return found;
}

calculate_status_e ecm(mpz_t factor, const mpz_t n) {
    if (mpz_even_p(n)) {
        mpz_set_ui(factor, 2);
        return CALCULATE_SUCCESS;
    }

    unsigned long B1, B2;
    int max_curves;
    choose_bounds(n, &B1, &B2, &max_curves);

    LOG_DEBUG("ECM-Montgomery: digits=%zu B1=%lu B2=%lu", mpz_sizeinbase(n, 10),
              B1, B2);

    int prime_count = 0;
    int* primes = sieve_primes((int)B1, &prime_count);

    LOG_DEBUG("ECM: sieve_primes done, prime_count=%d", prime_count);

    char* composite = calloc(B2 + 1, 1);
    if (composite == NULL) {
        LOG_ERROR("Fatal error: cannot allocate memory for composite array.");
        free(primes);
        return CALCULATE_ERR_MEMORY;
    }

    composite[0] = composite[1] = 1;
    for (unsigned long i = 2; i * i <= B2; i++) {
        if (!composite[i])
            for (unsigned long j = i * i; j <= B2; j += i)
                composite[j] = 1;
    }

    LOG_DEBUG("ECM: composite sieve done, starting curves B1=%lu B2=%lu", B1,
              B2);

    mpz_t A24, sigma;
    mpz_inits(A24, sigma, NULL);

    int found = 0;
    int stage1_ok = 0, stage2_ok = 0, stage1_fail = 0;

    for (int curve = 0; curve < max_curves; curve++) {
        LOG_DEBUG("ECM: curve %d", curve);

        mpz_urandomm(sigma, g_rstate, n);
        if (mpz_cmp_ui(sigma, 6) < 0)
            mpz_set_ui(sigma, 6);

        mont_point_t Q;
        mont_point_init(&Q);

        if (suyama_param(A24, &Q, sigma, n) < 0) {
            mont_point_clear(&Q);
            continue;
        }

        int ret = ecm_stage1(factor, n, &Q, A24, primes, prime_count, B1);

        if (ret == 1) {
            stage1_ok++;
            mont_point_clear(&Q);
            found = 1;
            break;
        }
        if (ret == -1) {
            stage1_fail++;
            mont_point_clear(&Q);
            continue;
        }

        if (ecm_stage2(factor, n, &Q, A24, composite, B1, B2)) {
            stage2_ok++;
            mont_point_clear(&Q);
            found = 1;
            break;
        }

        mont_point_clear(&Q);
    }

    LOG_DEBUG(
        "ECM-Montgomery stats: stage1_ok=%d stage2_ok=%d stage1_fail=%d "
        "found=%d",
        stage1_ok, stage2_ok, stage1_fail, found);

    if (!found) {
        mpz_set(factor, n);

        mpz_clears(A24, sigma, NULL);
        free(composite);
        free(primes);

        return CALCULATE_NOT_FOUND;
    }

    mpz_clears(A24, sigma, NULL);
    free(composite);
    free(primes);

    return CALCULATE_SUCCESS;
}