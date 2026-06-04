#pragma once

#include <gmp.h>

extern gmp_randstate_t g_rstate;

enum calculate_status_e;

enum calculate_status_e initialize_rstate();
void cleanup_rstate();