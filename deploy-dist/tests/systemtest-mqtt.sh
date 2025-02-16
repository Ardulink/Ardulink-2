#!/bin/bash

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

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

TEMP_DIR=$(mktemp -d)
ARDULINK_DIR="$TEMP_DIR/ArdulinkProtocol"
DEVICE=$(find_first_unused_device "/dev/ttyUSB")
PIN="12"

# Function to clean up containers and processes on exit
cleanup() {
    echo "Cleaning up..."

    echo "Stopping Java process..."
    kill $JAVA_PID

    echo "Stopping Docker Compose services..."
    docker compose -f "$COMPOSE_FILE" down

    echo "Removing temporary directory..."
    rm -rf "$TEMP_DIR"
}

find_unused_port() {
    local base_port=${1:-49152}
    local max_port=65535
    for port in $(seq $base_port $max_port); do
        if ! nc -z localhost "$port" 2>/dev/null; then
            echo "$port"
            return 0
        fi
    done
    echo "No available ports found in the range $base_port-$max_port"
    return 1
}

wait_for_port() {
    local port=$1
    local timeout=${2:-10}
    local host=${3:-localhost}

    local start_time=$(date +%s)
    while ! nc -z "$host" "$port"; do
        local current_time=$(date +%s)
        local elapsed_time=$((current_time - start_time))

        if [ "$elapsed_time" -ge "$timeout" ]; then
            echo "Timeout reached. Port $port on $host did not become available."
            return 1
        fi

        sleep 1
    done
    return 0
}

trap cleanup EXIT INT TERM

WS_PORT=$(find_unused_port 8000)
if [ -z "$WS_PORT" ]; then
    echo "Could not find an available port."
    exit 1
fi

# Step 1: Download the file and place it in the "ArdulinkProtocol" directory
echo "Downloading ArdulinkProtocol.ino..."
mkdir -p "$ARDULINK_DIR"
wget -qO "$ARDULINK_DIR/ArdulinkProtocol.ino.hex" https://github.com/Ardulink/Firmware/releases/download/v1.2.0/ArdulinkProtocol.ino.hex

# Step 2: Run the Docker container that emulates the Arduino
echo "Running Docker container for ArdulinkProtocol..."
export WS_PORT
export DEVICE
export UID
export ARDULINK_DIR
docker compose -f "$COMPOSE_FILE" up -d virtualavr

echo "Waiting for virtualavr to become healthy..."
until [ "$(docker inspect --format='{{.State.Health.Status}}' virtualavr)" == "healthy" ]; do
    sleep 1
done

# Step 3: Start websocat container
docker compose -f "$COMPOSE_FILE" up -d websocat
echo "WebSocket container started"

# Enable listening on pin $PIN
echo '{ "type": "pinMode", "pin": "'$PIN'", "mode": "digital" }' | docker compose -f "$COMPOSE_FILE" run --rm -T websocat-send-once "cat - | websocat ws://localhost:$WS_PORT"

# Step 4: Run the Java application in the background (detached mode)
MQTT_PORT=$(find_unused_port 1883)
if [ -z "$MQTT_PORT" ]; then
    echo "Could not find an available port."
    exit 1
fi

echo "Starting Ardulink MQTT service on port $MQTT_PORT..."
cd $SCRIPT_DIR/../target/ardulink/lib/
java -jar ardulink-mqtt-*.jar -standalone -brokerPort=$MQTT_PORT -connection "ardulink://serial?port=$DEVICE" &
JAVA_PID=$!
echo "Ardulink-MQTT started"
cd - >/dev/null

if wait_for_port $MQTT_PORT 10; then
    echo "Ardulink-MQTT server is ready on port $MQTT_PORT."
else
    echo "Failed to detect Ardulink-MQTT server on port $MQTT_PORT."
    exit 1
fi

# Step 5: Publish MQTT message and verify the response in the WebSocket container log file with a timeout
echo "Verifying WebSocket container response within 10 seconds..."
START_TIME=$(date +%s)
TIMEOUT=10

# Step 6: Define the MQTT topic and message dynamically
export MQTT_HOST="localhost"
export MQTT_PORT
export MQTT_TOPIC="home/devices/ardulink/D$PIN"
export MQTT_MESSAGE="true"

while true; do
    echo "Publishing MQTT message to set pin state..."
    docker compose -f "$COMPOSE_FILE" run --rm mqtt-pub-once

    # Check the WebSocket output file for the expected message
    if docker compose -f "$COMPOSE_FILE" logs websocat | jq -R -e 'split(" | ") | .[1] | fromjson? | select(.type == "pinState" and .pin == "'$PIN'" and .state == true)' >/dev/null 2>&1; then
        echo "Test passed. Received the expected WebSocket message."
        break
    fi

    # Check if we exceeded the timeout (10 seconds)
    CURRENT_TIME=$(date +%s)
    ELAPSED_TIME=$((CURRENT_TIME - START_TIME))
    if [ $ELAPSED_TIME -ge $TIMEOUT ]; then
        echo "Test failed. Timeout reached without receiving the expected message."
        exit 1
    fi

    sleep 1
done

# If everything is successful, cleanup will be called automatically when the script exits
echo "Test completed successfully."
