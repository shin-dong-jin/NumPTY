#define _GNU_SOURCE

#include "base/signal_handler.h"

#include "common.h"
#include "logger.h"

volatile sig_atomic_t keep_running = 1;

static void handle_shutdown(int signum) {
    (void)signum;

    keep_running = 0;
}

static void handle_log_level(int signum) {
    (void)signum;

    set_log_level(g_current_log_level == LOG_LEVEL_DEBUG  ? LOG_LEVEL_INFO
                  : g_current_log_level == LOG_LEVEL_INFO ? LOG_LEVEL_WARN
                  : g_current_log_level == LOG_LEVEL_WARN ? LOG_LEVEL_ERROR
                                                          : LOG_LEVEL_DEBUG);

    switch (g_current_log_level) {
        case LOG_LEVEL_DEBUG:
            LOG_DEBUG("Target Log level changed to %s.", get_log_level_name(g_current_log_level));
            break;
        case LOG_LEVEL_INFO:
            LOG_INFO("Target Log level changed to %s.", get_log_level_name(g_current_log_level));
            break;
        case LOG_LEVEL_WARN:
            LOG_WARN("Target Log level changed to %s.", get_log_level_name(g_current_log_level));
            break;
        case LOG_LEVEL_ERROR:
            LOG_ERROR("Target Log level changed to %s.", get_log_level_name(g_current_log_level));
            break;
    }
}

signal_status_e register_shutdown_handlers() {
    struct sigaction sa;

    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;

    sa.sa_handler = handle_shutdown;
    if (sigaction(SIGINT, &sa, NULL) == -1 || sigaction(SIGTERM, &sa, NULL) == -1) {
        LOG_ERROR("Fatal error: Could not register SHUTDOWN handler.");
        return SIGNAL_ERR;
    }

    sa.sa_handler = handle_log_level;
    if (sigaction(SIGUSR1, &sa, NULL) == -1) {
        LOG_ERROR("Fatal error: Could not register LOG_LEVEL handler.");
        return SIGNAL_ERR;
    }

    LOG_INFO("Shutdown Handlers registered.");
    return SIGNAL_SUCCESS;
}