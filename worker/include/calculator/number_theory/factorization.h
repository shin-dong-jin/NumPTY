#pragma once

#define POLLARD_RHO_MAX_STEPS 1000000UL

#include <gmp.h>

enum calculate_status_e;

enum calculate_status_e factorize(const mpz_t n, mpz_t* temp, int* index);