#pragma once

typedef enum calculate_status_e {
    CALCULATE_TIMEOUT = 2,
    CALCULATE_NOT_FOUND = 1,
    CALCULATE_SUCCESS = 0,
    CALCULATE_ERR_INPUT = -1,
    CALCULATE_ERR_MEMORY = -2,
    CALCULATE_ERR_ARGS = -3,
} calculate_status_e;