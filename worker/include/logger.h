#pragma once

#define __FILENAME__                                     \
    (strrchr(__FILE__, '/') ? strrchr(__FILE__, '/') + 1 \
                            : (strrchr(__FILE__, '\\') ? strrchr(__FILE__, '\\') + 1 : __FILE__))

#define COLOR_RESET "\x1b[0m"
#define COLOR_DEBUG "\x1b[0m"
#define COLOR_INFO "\x1b[34m"
#define COLOR_WARN "\x1b[31m"
#define COLOR_ERROR "\x1b[1;31m"
#define COLOR_LOGGER "\x1b[36m"

typedef enum logger_status_e {
    LOGGER_SUCCESS = 0,
    LOGGER_ERR_ARGS = -1,
    LOGGER_ERR_FILE = -2,
    LOGGER_ERR_LEVEL = -3
} logger_status_e;

typedef enum log_level_e {
    LOG_LEVEL_DEBUG = 0,
    LOG_LEVEL_INFO = 1,
    LOG_LEVEL_WARN = 2,
    LOG_LEVEL_ERROR = 3
} log_level_e;

extern log_level_e g_current_log_level;

const char* get_log_level_name(log_level_e log_level);

logger_status_e init_logger(const char* exec_path);

logger_status_e set_log_level(log_level_e log_level);

void log_message(log_level_e level, const char* level_str, const char* color_code, const char* file,
                 int line, const char* fmt, ...);

#define LOG_OUT(level, level_str, color_code, fmt, ...)                                            \
    do {                                                                                           \
        if (level >= g_current_log_level) {                                                        \
            log_message(level, level_str, color_code, __FILENAME__, __LINE__, fmt, ##__VA_ARGS__); \
        }                                                                                          \
    } while (0)

#define LOG_DEBUG(fmt, ...) LOG_OUT(LOG_LEVEL_DEBUG, "DEBUG", COLOR_DEBUG, fmt, ##__VA_ARGS__)
#define LOG_INFO(fmt, ...) LOG_OUT(LOG_LEVEL_INFO, "INFO", COLOR_INFO, fmt, ##__VA_ARGS__)
#define LOG_WARN(fmt, ...) LOG_OUT(LOG_LEVEL_WARN, "WARN", COLOR_WARN, fmt, ##__VA_ARGS__)
#define LOG_ERROR(fmt, ...) LOG_OUT(LOG_LEVEL_ERROR, "ERROR", COLOR_ERROR, fmt, ##__VA_ARGS__)
