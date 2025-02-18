#!/bin/bash

# Function to print an error message and exit with status 1
die() {
    echo "Error: $1"
    exit 1
}

# Function to clean up containers and processes on exit
cleanup() {
    echo "Cleaning up..."

    echo "Stopping Java process..."
    kill $JAVA_PID 2>/dev/null

    echo "Stopping Docker Compose services..."
    docker compose -f "$COMPOSE_FILE" down

    echo "Removing temporary directory..."
    rm -rf "$TEMP_DIR"
}

# Function to find the first unused device
find_first_unused_device() {
    local base_path="$1"
    local index=0
    while true; do
        if [ ! -e "${base_path}${index}" ]; then
            echo "${base_path}${index}"
            return 0
        fi
        index=$((index + 1))
    done
}

# Function to find an unused port
find_unused_port() {
    local base_port=${1:-49152}
    local max_port=65535
    for port in $(seq $base_port $max_port); do
        if ! nc -z localhost "$port" 2>/dev/null; then
            echo "$port"
            return 0
        fi
    done
    die "No available ports found in the range $base_port-$max_port"
}

# Function to wait for a port to become available
wait_for_port() {
    local port=$1
    local timeout=${2:-10}
    local host=${3:-localhost}

    local start_time=$(date +%s)
    while ! nc -z "$host" "$port"; do
        local current_time=$(date +%s)
        local elapsed_time=$((current_time - start_time))

        if [ "$elapsed_time" -ge "$timeout" ]; then
            die "Timeout reached. Port $port on $host did not become available."
        fi

        sleep 1
    done
    return 0
}

wait_for_container_healthy() {
    local container_name="$1"
    echo "Waiting for $container_name to become healthy..."
    until [ "$(docker compose -f "$COMPOSE_FILE" ps --format='{{json .Health }}' "$container_name")" == '"healthy"' ]; do
        sleep 1
    done
    echo "$container_name is healthy."
}

check_websocket_message() {
    local action="$1"         # Command to execute the action
    local json_pattern="$2"   # JQ pattern to match in the WebSocket message
    local timeout="${3:-10}"  # Default timeout of 10 seconds if not specified

    echo "Verifying WebSocket container response within $timeout seconds..."
    START_TIME=$(date +%s)

    while true; do
        eval "$action"

        # Check WebSocket logs for the expected message using the provided jq pattern
        if docker compose -f "$COMPOSE_FILE" logs websocat | jq -R -e \
            "split(\" | \") | .[1] | fromjson? | select($json_pattern)" \
            >/dev/null 2>&1; then
            echo "Test passed. Received WebSocket message matching $json_pattern."
            break
        fi

        # Timeout check
        CURRENT_TIME=$(date +%s)
        ELAPSED_TIME=$((CURRENT_TIME - START_TIME))
        [ $ELAPSED_TIME -ge $timeout ] && die "Test failed. Timeout reached without receiving the expected message."

        sleep 1
    done
}
