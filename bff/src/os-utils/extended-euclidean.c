#define _GNU_SOURCE
#include <libgen.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char* argv[]) {
    char exe_path[PATH_MAX];

    ssize_t len = readlink("/proc/self/exe", exe_path, sizeof(exe_path) - 1);
    if (len == -1) {
        return 1;
    }
    exe_path[len] = '\0';

    char dir_path[PATH_MAX];
    strcpy(dir_path, exe_path);
    char* bin_dir = dirname(dir_path);

    char node_path[PATH_MAX];
    char script_path[PATH_MAX];

    snprintf(node_path, sizeof(node_path), "%s/../usr/bin/node", bin_dir);
    snprintf(script_path, sizeof(script_path),
             "%s/../usr/libexec/extended-euclidean.js", bin_dir);

    char* exec_args[argc + 2];
    exec_args[0] = "node";
    exec_args[1] = script_path;
    for (int i = 1; i < argc; i++) {
        exec_args[i + 1] = argv[i];
    }
    exec_args[argc + 1] = NULL;

    execv(node_path, exec_args);
    return 1;
}