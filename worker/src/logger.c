#define _GNU_SOURCE
#include "logger.h"

#include <stdarg.h>
#include <unistd.h>

#include "common.h"

static char process_name[256] = "Unknown";
log_level_e g_current_log_level = LOG_LEVEL_INFO;

const char* get_log_level_name(log_level_e log_level) {
    switch (log_level) {
        case LOG_LEVEL_DEBUG:
            return "DEBUG";
        case LOG_LEVEL_INFO:
            return "INFO";
        case LOG_LEVEL_WARN:
            return "WARN";
        case LOG_LEVEL_ERROR:
            return "ERROR";
        default:
            return "UNKNOWN";
    }

    return "UNKNOWN";
}

logger_status_e init_logger(const char* exec_path) {
    if (exec_path == NULL) {
        return LOGGER_ERR_ARGS;
    }

    const char* slash = strrchr(exec_path, '/');

    if (!slash) {
        slash = strrchr(exec_path, '\\');
    }

    const char* name = slash ? slash + 1 : exec_path;

    strncpy(process_name, name, sizeof(process_name) - 1);
    process_name[sizeof(process_name) - 1] = '\0';

    return LOGGER_SUCCESS;
}

logger_status_e set_log_level(log_level_e log_level) {
    if (log_level < LOG_LEVEL_DEBUG || log_level > LOG_LEVEL_ERROR) {
        g_current_log_level = LOG_LEVEL_INFO;
        return LOGGER_ERR_LEVEL;
    }

    g_current_log_level = log_level;
    return LOGGER_SUCCESS;
}

void log_message(log_level_e level, const char* level_str, const char* color_code, const char* file,
                 int line, const char* fmt, ...) {
    FILE* out_stream = level >= LOG_LEVEL_ERROR ? stderr : stdout;

    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);

    struct tm tm_info;
    localtime_r(&ts.tv_sec, &tm_info);
    char time_buf[32];
    strftime(time_buf, sizeof(time_buf), "%Y-%m-%dT%H:%M:%S", &tm_info);

    int milliseconds = ts.tv_nsec / 1000000;

    pid_t pid = getpid();

    fprintf(out_stream, "%s.%03d [%s:%d] %s%s%s %s%s:%d%s - ", time_buf, milliseconds, process_name,
            pid, color_code, level_str, COLOR_RESET, COLOR_LOGGER, file, line, COLOR_RESET);

    va_list args;
    va_start(args, fmt);
    vfprintf(out_stream, fmt, args);
    va_end(args);

    fprintf(out_stream, "\n");
    fflush(out_stream);
}