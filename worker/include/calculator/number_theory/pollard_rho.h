#pragma once

#define BATCH_GCD 128

#include <gmp.h>

enum calculate_status_e;

enum calculate_status_e pollard_rho(mpz_t factor,
                                    const mpz_t n,
                                    unsigned long max_steps);