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

# Function to clean up containers and processes on exit
cleanup() {
    echo "Cleaning up..."
    
    echo "Stopping Java process..."
    kill $JAVA_PID
        
    echo "Stopping WebSocket container..."
    docker stop $WS_CONTAINER_ID > /dev/null
    
    echo "Stopping virtualavr container..."
    docker stop $VIRTUALAVR_CONTAINER_ID > /dev/null
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
VIRTUALAVR_CONTAINER_ID=$(docker run --rm -d -p $WS_PORT:8080 -e VIRTUALDEVICE=$DEVICE -e DEVICEUSER=$UID -e FILENAME=ArdulinkProtocol.ino -v /dev:/dev -v ./deploy-dist/rootfolder/sketches/ArdulinkProtocol:/sketch pfichtner/virtualavr)

#VIRTUALAVR_CONTAINER_ID=$(docker run --rm -d -p $WS_PORT:8080 -e VIRTUALDEVICE=$DEVICE -e FILENAME=ArdulinkProtocol.ino -v /dev:/dev -v "$ARDULINK_DIR":/sketch pfichtner/virtualavr)

if wait_for_port $WS_PORT 10; then
    echo "WebSocket server is ready on port $WS_PORT."
else
    echo "Failed to detect WebSocket server on port $WS_PORT."
    exit 1
fi

# Step 3: Run the WebSocket container in the background (detached mode)
# Run WebSocket container in detached mode and connect to WebSocket server with the -c option
echo "Running WebSocket container in detached mode and connecting to ws://localhost:$WS_PORT..."
WS_CONTAINER_ID=$(docker run --rm --net=host -d -i solsson/websocat ws://localhost:$WS_PORT)
echo "WebSocket container started"

# Enable listening on pin $PIN
echo '{ "type": "pinMode", "pin": "'$PIN'", "mode": "digital" }' | docker run --rm --net=host -i solsson/websocat ws://localhost:$WS_PORT

# Step 4: Run the Java application in the background (detached mode)
REST_PORT=$(find_unused_port 8080)
if [ -z "$REST_PORT" ]; then
    echo "Could not find an available port."
    exit 1
fi

echo "Starting Ardulink REST service on port $REST_PORT..."
cd ./deploy-dist/target/ardulink/lib/
java -jar ardulink-rest-*.jar -port=$REST_PORT -connection "ardulink://serial?port=$DEVICE" &
JAVA_PID=$!
echo "Ardulink-REST started"
cd - > /dev/null

if wait_for_port $REST_PORT 10; then
    echo "Ardulink-REST server is ready on port $REST_PORT."
else
    echo "Failed to detect Ardulink-REST server on port $REST_PORT."
    exit 1
fi

# Step 5: Call the API endpoint
echo "Calling the API endpoint to set pin state..."
RESPONSE=$(curl -s -X 'PUT' \
  "http://localhost:$REST_PORT/pin/digital/$PIN" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/text' \
  -d 'true')

# this is an e2e-/systemtest so we don't check the response here
#if [[ "$RESPONSE" == *"alp://dred/$PIN/1=OK"* ]]; then
#    echo "Test passed. Received the expected response."
#else
#    echo "Test failed. Expected response not found, received: $RESPONSE."
#    exit 1
#fi

# Step 6: Verify the response in the WebSocket container log file with a timeout
echo "Verifying WebSocket container response within 10 seconds..."
START_TIME=$(date +%s)
TIMEOUT=10

while true; do
    # Check the WebSocket output file for the expected message
    if docker logs "$WS_CONTAINER_ID" | jq -e '. | select(.type == "pinState" and .pin == "'$PIN'" and .state == true)' > /dev/null 2>&1; then
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
