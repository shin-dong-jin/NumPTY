#pragma once

#define ECM_D 210
#define ECM_MAX_CURVES 100

#include <gmp.h>

enum calculate_status_e;

enum calculate_status_e ecm(mpz_t factor, const mpz_t n);