#pragma once

#include <gmp.h>

void trial_division(const mpz_t n, mpz_t remaining, mpz_t* temp, int* index);