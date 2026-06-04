.PHONY: setup build run clean rebuild

setup:
	cmake -B build

build:
	cmake --build build

run: build
	@echo "\033[1;32mStarting worker with dev configurations...\033[0m"
	REDIS_HOST="127.0.0.1" \
	REDIS_PORT=6379 \
	REDIS_PASSWORD="" \
	CONSUMER_GROUP="cg-worker" \
	STREAM_REQUEST="numpty:was:request" \
	STREAM_RESPONSE="numpty:worker:response" \
	BLOCK_TIMEOUT_MS=1 \
	BANNER_PATH="resource/banner.logo" \
	TARGET_LOG_LEVEL=1 \
	./build/worker

clean:
	rm -rf build
	@echo "\033[1;32mBuild cleaned successfully.\033[0m"

rebuild: clean setup build
	@echo "\033[1;32mClean, setup, and build completed.\033[0m"