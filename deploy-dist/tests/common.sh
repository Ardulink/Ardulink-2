#!/bin/bash

# Function to generate a unique stack ID for Docker Compose
generate_stack_id() {
    # Using timestamp as unique identifier
    date +%s%N | cut -b1-13
}

# Generated once when this file is sourced at script startup, so it stays unique
# and unchanged for the whole run. Every docker compose call must pass it via -p.
STACK_ID="$(generate_stack_id)"
export STACK_ID

# Function to print an error message and exit with status 1
die() {
    echo "Error: $1"
    exit 1
}

# Function to clean up containers and processes on exit
cleanup() {
    echo "Cleaning up..."

    echo "Stopping Java process..."
    kill "$JAVA_PID" 2>/dev/null

    echo "Stopping Docker Compose services..."
    docker compose -p "$STACK_ID" -f "$COMPOSE_FILE" down

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
    local base_port="${1:-49152}"
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
    timeout "$timeout" bash -c 'until nc -z "'"$host"'" "'"$port"'"; do sleep 1; done' || die "Timeout reached. Port $port on $host did not become available."
}

wait_for_container_healthy() {
    local container_name="$1"
    local timeout="${2:-30}"

    echo "Waiting for $container_name to become healthy..."
    timeout "$timeout" bash -c '
        until [ "$(docker compose -p "'"$STACK_ID"'" -f "'"$COMPOSE_FILE"'" ps --format "{{json .Health }}" "'"$container_name"'")" = "\"healthy\"" ]; do sleep 1; done' || die "Timeout reached. Container $container_name did not become healthy."
}

# Function to resolve everything that depends on the protocol under test. Sets
# PROTO_FILENAME (the firmware flashed into the emulated Arduino),
# PROTO_FIRMWARE_SOURCE (where that firmware comes from) and PROTO_CONNECTION (the
# connection string used by the applications).
resolve_protocol() {
    local protocol="${1:-ardulink}"
    local virtualdevice="$2"

    PROTO_CONNECTION="ardulink://serial?port=$virtualdevice"
    case "$protocol" in
        ardulink)
            # The distribution does not ship the sketch anymore, so the firmware is taken from the
            # releases of the Firmware repository. Downloading it proves that the hex users flash
            # from the release page still works with this distribution, which the copy the
            # integration tests use would not, as that copy never leaves the repository.
            PROTO_FILENAME="ArdulinkProtocol.ino.hex"
            PROTO_FIRMWARE_SOURCE="https://github.com/Ardulink/Firmware/releases/download/v1.2.0/ArdulinkProtocol.ino.hex"
            PROTO_CONNECTION="$PROTO_CONNECTION"
            ;;
        firmata)
            # Stock Firmata is no Ardulink firmware, hence the Firmware repository does not publish
            # it and there is nothing to download. Its hex is taken from the resources the
            # integration tests use instead.
            #
            # proto=Firmata is only shipped next to the applications in the distribution, not on their
            # classpath. Selecting it therefore proves that a protocol is discovered from the module
            # directory (ardulink.module.dir, defaulting to the working directory) next to the jar.
            PROTO_FILENAME="StandardFirmata.hex"
            PROTO_FIRMWARE_SOURCE="$SCRIPT_DIR/../../ardulink-core-base/src/test/resources/firmware/$PROTO_FILENAME"
            PROTO_CONNECTION="$PROTO_CONNECTION&proto=Firmata&baudrate=9600"
            ;;
        *)
            die "Unknown protocol '$protocol'. Supported protocols: ardulink, firmata."
            ;;
    esac
}

# Function to place the firmware of the resolved protocol in the directory mounted
# as sketch by the virtualavr container, downloading it or copying it from the
# repository depending on PROTO_FIRMWARE_SOURCE.
install_firmware() {
    mkdir -p "$ARDULINK_DIR"

    case "$PROTO_FIRMWARE_SOURCE" in
        http*)
            echo "Downloading $PROTO_FILENAME..."
            wget -qO "$ARDULINK_DIR/$PROTO_FILENAME" "$PROTO_FIRMWARE_SOURCE" \
                || die "Failed to download $PROTO_FILENAME from $PROTO_FIRMWARE_SOURCE"
            ;;
        *)
            echo "Preparing $PROTO_FILENAME..."
            [ -f "$PROTO_FIRMWARE_SOURCE" ] || die "Firmware not found: $PROTO_FIRMWARE_SOURCE"
            cp "$PROTO_FIRMWARE_SOURCE" "$ARDULINK_DIR/$PROTO_FILENAME"
            ;;
    esac
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
        if docker compose -p "$STACK_ID" -f "$COMPOSE_FILE" logs websocat | jq -R -e \
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
