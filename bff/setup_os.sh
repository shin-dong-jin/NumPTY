#!/bin/bash

OS_DIR="./os"
SRC_DIR="./src"

echo "Setting up OS directories..."

rm -rf $OS_DIR/bin
rm -f $OS_DIR/usr/bin/node

mkdir -p $OS_DIR/bin $OS_DIR/usr/bin $OS_DIR/usr/libexec $OS_DIR/home/user/workspace $OS_DIR/etc

ln -sf $(which clear) $OS_DIR/bin/clear
ln -sf $(which curl) $OS_DIR/bin/curl
ln -sf $(which date) $OS_DIR/bin/date
ln -sf $(which jq) $OS_DIR/bin/jq
ln -sf $(which ls) $OS_DIR/bin/ls
ln -sf $(which starship) $OS_DIR/bin/starship
ln -sf $(which uname) $OS_DIR/bin/uname
ln -sf $(which uptime) $OS_DIR/bin/uptime

ln -sf $(which node) $OS_DIR/usr/bin/node

echo "Compiling C wrapper binaries..."

gcc -o $OS_DIR/bin/extended-euclidean $SRC_DIR/os-utils/extended-euclidean.c
gcc -o $OS_DIR/bin/factorize $SRC_DIR/os-utils/factorize.c
gcc -o $OS_DIR/bin/gcd $SRC_DIR/os-utils/gcd.c
gcc -o $OS_DIR/bin/lcm $SRC_DIR/os-utils/lcm.c
gcc -o $OS_DIR/bin/primality-test $SRC_DIR/os-utils/primality-test.c

echo "OS settings completed."