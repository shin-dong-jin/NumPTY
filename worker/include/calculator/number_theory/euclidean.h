#pragma once

#include <gmp.h>

void gcd(mpz_t gcd, const mpz_t target_a, const mpz_t target_b);
void lcm(mpz_t lcm, const mpz_t target_a, const mpz_t target_b);
void gcd_recursive(mpz_t gcd, const mpz_t a, const mpz_t b);
void lcm_recursive(mpz_t lcm,
                   const mpz_t a,
                   const mpz_t b,
                   const mpz_t target_a,
                   const mpz_t target_b);
void extended_euclidean(mpz_t gcd,
                        mpz_t x,
                        mpz_t y,
                        const mpz_t a,
                        const mpz_t b);
int modulo_inverse(mpz_t inverse, const mpz_t target, const mpz_t modulo);