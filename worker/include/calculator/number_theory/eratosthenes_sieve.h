#pragma once

#define TRIAL_DIVISION_LIMIT 1000000

extern int* g_small_primes;
extern int g_small_prime_count;

enum calculate_status_e;

enum calculate_status_e initialize_sieve();
void cleanup_sieve();