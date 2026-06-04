cmake_minimum_required(VERSION 3.10)

project(CalculateWorker C)

add_executable(worker
    src/main.c
    src/worker.c
    src/logger.c
    src/base/config.c
    src/base/protocol.c
    src/base/signal_handler.c
    src/calculator/gmp_random.c
    src/calculator/number_theory/euclidean.c
    src/calculator/number_theory/exec_number_theory.c
    src/calculator/number_theory/factorization.c
    src/calculator/number_theory/trial_division.c
    src/calculator/number_theory/pollard_rho.c
    src/calculator/number_theory/ecm.c
    src/calculator/number_theory/eratosthenes_sieve.c
    src/calculator/number_theory/miller_rabin.c
    src/calculator/sorting/sorting.c
    src/redis/redis_client.c
    src/redis/redis_queue.c
    src/redis/redis_stream.c
    src/transport/egress.c
    src/transport/ingress.c
)

target_include_directories(worker PUBLIC include)

target_compile_options(worker PRIVATE -Wall -Wextra)

target_link_libraries(worker PUBLIC hiredis gmp)