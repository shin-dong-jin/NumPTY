#pragma once

#define MAX_FACTORS 256

struct base_task_t;
enum calculate_status_e;

enum calculate_status_e exec_gcd(struct base_task_t* base);
enum calculate_status_e exec_lcm(struct base_task_t* base);
enum calculate_status_e exec_extended_euclidean(struct base_task_t* base);
enum calculate_status_e exec_primality_test_miller_rabin(
    struct base_task_t* base);
enum calculate_status_e exec_factorize(struct base_task_t* base);