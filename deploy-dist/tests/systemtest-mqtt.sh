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

TEMP_DIR=$(mktemp -d)
ARDULINK_DIR="$TEMP_DIR/ArdulinkProtocol"
DEVICE=$(find_first_unused_device "/dev/ttyUSB")
PIN="12"

DOCKER_IMAGE_VIRTUALAVR="pfichtner/virtualavr"
DOCKER_IMAGE_WEBSOCAT="solsson/websocat"
DOCKER_IMAGE_MQTT_PUB="efrecon/mqtt-client"

# Function to clean up containers and processes on exit
cleanup() {
    echo "Cleaning up..."

    echo "Stopping Java process..."
    kill $JAVA_PID

    echo "Stopping WebSocket container..."
    docker stop $WS_CONTAINER_ID >/dev/null

    echo "Stopping virtualavr container..."
    docker stop $VIRTUALAVR_CONTAINER_ID >/dev/null

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
#echo "Downloading ArdulinkProtocol.ino..."
#mkdir -p "$ARDULINK_DIR"
#curl -o "$ARDULINK_DIR/ArdulinkProtocol.ino" https://raw.githubusercontent.com/Ardulink/Firmware/refs/heads/main/ArdulinkProtocol/ArdulinkProtocol.ino

# Step 2: Run the Docker container that emulates the Arduino
echo "Running Docker container for ArdulinkProtocol..."
docker pull $DOCKER_IMAGE_VIRTUALAVR # download or update (if cached an newer version is available)
VIRTUALAVR_CONTAINER_ID=$(docker run --rm -d -p $WS_PORT:8080 -e VIRTUALDEVICE=$DEVICE -e DEVICEUSER=$UID -e FILENAME=ArdulinkProtocol.ino -v /dev:/dev -v ./deploy-dist/rootfolder/sketches/ArdulinkProtocol:/sketch $DOCKER_IMAGE_VIRTUALAVR)

#VIRTUALAVR_CONTAINER_ID=$(docker run --rm -d -p $WS_PORT:8080 -e VIRTUALDEVICE=$DEVICE -e FILENAME=ArdulinkProtocol.ino -v /dev:/dev -v "$ARDULINK_DIR":/sketch $DOCKER_IMAGE_VIRTUALAVR)

if wait_for_port $WS_PORT 10; then
    echo "WebSocket server is ready on port $WS_PORT."
else
    echo "Failed to detect WebSocket server on port $WS_PORT."
    exit 1
fi

# Step 3: Run the WebSocket container in the background (detached mode)
# Run WebSocket container in detached mode and connect to WebSocket server with the -c option
echo "Running WebSocket container in detached mode and connecting to ws://localhost:$WS_PORT..."
docker pull $DOCKER_IMAGE_WEBSOCAT # download or update (if cached an newer version is available)
WS_CONTAINER_ID=$(docker run --rm --net=host -d -i $DOCKER_IMAGE_WEBSOCAT ws://localhost:$WS_PORT)
echo "WebSocket container started"

# Enable listening on pin $PIN
echo '{ "type": "pinMode", "pin": "'$PIN'", "mode": "digital" }' | docker run --rm --net=host -i $DOCKER_IMAGE_WEBSOCAT ws://localhost:$WS_PORT

MQTT_PORT=$(find_unused_port 1883)
if [ -z "$MQTT_PORT" ]; then
    echo "Could not find an available port."
    exit 1
fi

# Step 4: Run the Java application in the background (detached mode)
echo "Starting Ardulink MQTT service on port $MQTT_PORT..."
cd ./deploy-dist/target/ardulink/lib/
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

# Step 6: Publish MQTT message and verify the response in the WebSocket container log file with a timeout
echo "Verifying WebSocket container response within 10 seconds..."
START_TIME=$(date +%s)
TIMEOUT=10

while true; do
    echo "Publishing MQTT message to set pin state..."
    docker pull $DOCKER_IMAGE_MQTT_PUB # download or update (if cached an newer version is available)
    docker run --rm --net=host $DOCKER_IMAGE_MQTT_PUB pub -h localhost -p $MQTT_PORT -i 'efrecon-mqtt-client' -t "home/devices/ardulink/D$PIN" -m 'true'

    # Check the WebSocket output file for the expected message
    if docker logs "$WS_CONTAINER_ID" | jq -e '. | select(.type == "pinState" and .pin == "'$PIN'" and .state == true)' >/dev/null 2>&1; then
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
