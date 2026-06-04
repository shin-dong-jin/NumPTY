#pragma once

#include <signal.h>

typedef enum signal_status_e { SIGNAL_SUCCESS = 0, SIGNAL_ERR = -1 } signal_status_e;

extern volatile sig_atomic_t keep_running;

signal_status_e register_shutdown_handlers();