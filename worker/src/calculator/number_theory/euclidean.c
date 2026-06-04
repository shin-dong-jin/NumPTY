#include "calculator/number_theory/euclidean.h"

#include "common.h"
#include "logger.h"

void gcd(mpz_t gcd, const mpz_t target_a, const mpz_t target_b) {
    mpz_t a, b, temp;
    mpz_inits(a, b, temp, NULL);
    mpz_set(a, target_a);
    mpz_set(b, target_b);

    while (mpz_cmp_ui(b, 0) != 0) {
        mpz_set(temp, b);
        mpz_mod(b, a, b);
        mpz_set(a, temp);
    }

    mpz_set(gcd, a);
    mpz_clears(a, b, temp, NULL);
}

void lcm(mpz_t lcm, const mpz_t target_a, const mpz_t target_b) {
    mpz_t a, b, temp;
    mpz_inits(a, b, temp, NULL);
    mpz_set(a, target_a);
    mpz_set(b, target_b);

    while (mpz_cmp_ui(b, 0) != 0) {
        mpz_set(temp, b);
        mpz_mod(b, a, b);
        mpz_set(a, temp);
    }

    mpz_divexact(lcm, target_a, a);
    mpz_mul(lcm, lcm, target_b);
    mpz_clears(a, b, temp, NULL);
}

void gcd_recursive(mpz_t gcd, const mpz_t a, const mpz_t b) {
    if (mpz_cmp_ui(b, 0) == 0) {
        mpz_set(gcd, a);
        return;
    }

    mpz_t a_mod_b;
    mpz_init(a_mod_b);
    mpz_mod(a_mod_b, a, b);
    gcd_recursive(gcd, b, a_mod_b);
    mpz_clear(a_mod_b);
}

void lcm_recursive(mpz_t lcm,
                   const mpz_t a,
                   const mpz_t b,
                   const mpz_t target_a,
                   const mpz_t target_b) {
    if (mpz_cmp_ui(b, 0) == 0) {
        mpz_divexact(lcm, target_a, a);
        mpz_mul(lcm, lcm, target_b);
        return;
    }

    mpz_t a_mod_b;
    mpz_init(a_mod_b);
    mpz_mod(a_mod_b, a, b);
    lcm_recursive(lcm, b, a_mod_b, target_a, target_b);
    mpz_clear(a_mod_b);
}

void extended_euclidean(mpz_t gcd,
                        mpz_t x,
                        mpz_t y,
                        const mpz_t a,
                        const mpz_t b) {
    mpz_t prev_r, r;
    mpz_t prev_s, s;
    mpz_t prev_t, t;
    mpz_t q, temp;

    mpz_inits(prev_r, r, prev_s, s, prev_t, t, q, temp, NULL);
    mpz_set(prev_r, a);
    mpz_set(r, b);
    mpz_set_ui(prev_s, 1);
    mpz_set_ui(s, 0);
    mpz_set_ui(prev_t, 0);
    mpz_set_ui(t, 1);

    while (mpz_cmp_ui(r, 0) != 0) {
        mpz_div(q, prev_r, r);

        mpz_set(temp, r);
        mpz_mul(r, q, r);
        mpz_sub(r, prev_r, r);
        mpz_set(prev_r, temp);

        mpz_set(temp, s);
        mpz_mul(s, q, s);
        mpz_sub(s, prev_s, s);
        mpz_set(prev_s, temp);

        mpz_set(temp, t);
        mpz_mul(t, q, t);
        mpz_sub(t, prev_t, t);
        mpz_set(prev_t, temp);
    }

    mpz_set(gcd, prev_r);
    mpz_set(x, prev_s);
    mpz_set(y, prev_t);

    mpz_clears(prev_r, r, prev_s, s, prev_t, t, q, temp, NULL);
}

int modulo_inverse(mpz_t inverse, const mpz_t target, const mpz_t modulo) {
    mpz_t gcd, x, y;
    mpz_inits(gcd, x, y, NULL);

    extended_euclidean(gcd, x, y, target, modulo);

    if (mpz_cmp_ui(gcd, 1) != 0) {
        mpz_clears(gcd, x, y, NULL);
        return 0;
    }

    mpz_mod(inverse, x, modulo);
    mpz_clears(gcd, x, y, NULL);
    return 1;
}