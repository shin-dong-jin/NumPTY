#define _GNU_SOURCE

#include "calculator/gmp_random.h"

#include "calculator/calculator.h"
#include "common.h"
#include "logger.h"

gmp_randstate_t g_rstate;
static int g_rstate_initialized = 0;

calculate_status_e initialize_rstate() {
    if (!g_rstate_initialized) {
        gmp_randinit_mt(g_rstate);
        struct timespec ts;

        if (clock_gettime(CLOCK_MONOTONIC, &ts) != 0) {
            LOG_WARN(
                "Failed to clock_gettime. gmp_randseed exec with time seed.");
            gmp_randseed_ui(g_rstate, (unsigned long)time(NULL));
        } else {
            gmp_randseed_ui(
                g_rstate, (unsigned long)ts.tv_nsec ^ (unsigned long)ts.tv_sec);
        }
        g_rstate_initialized = 1;
    }

    return CALCULATE_SUCCESS;
}

void cleanup_rstate() {
    if (g_rstate_initialized) {
        gmp_randclear(g_rstate);
        g_rstate_initialized = 0;
    }
}