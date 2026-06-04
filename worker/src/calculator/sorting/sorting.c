#include "calculator/sorting/sorting.h"

#include "common.h"
#include "logger.h"

static int partition_mpz(mpz_t* arr, int left, int right) {
    mpz_t* pivot = &arr[right];
    int i = left - 1;

    for (int j = left; j < right; j++) {
        if (mpz_cmp(arr[j], *pivot) <= 0) {
            i++;
            mpz_swap(arr[i], arr[j]);
        }
    }

    mpz_swap(arr[i + 1], arr[right]);
    return i + 1;
}

void quicksort_mpz(mpz_t* arr, int left, int right) {
    if (left < right) {
        int partition_index = partition_mpz(arr, left, right);
        quicksort_mpz(arr, left, partition_index - 1);
        quicksort_mpz(arr, partition_index + 1, right);
    }
}