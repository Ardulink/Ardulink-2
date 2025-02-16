#!/bin/bash

# Include the common script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/common.sh"

COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

TEMP_DIR=$(mktemp -d)
ARDULINK_DIR="$TEMP_DIR/ArdulinkProtocol"
DEVICE=$(find_first_unused_device "/dev/ttyUSB")
PIN="12"

trap cleanup EXIT INT TERM

WS_PORT=$(find_unused_port 8000)
[ -z "$WS_PORT" ] && die "Could not find an available port."

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
wait_for_container_healthy virtualavr

# Step 3: Start websocat container (listening for messages sent by virtualavr)
docker compose -f "$COMPOSE_FILE" up -d websocat
echo "WebSocket container started"

# Enable listening on pin $PIN
echo '{ "type": "pinMode", "pin": "'$PIN'", "mode": "digital" }' | docker compose -f "$COMPOSE_FILE" run --rm -T websocat-send-once "cat - | websocat ws://localhost:$WS_PORT"

# Step 4: Run the Java application in the background (detached mode)
REST_PORT=$(find_unused_port 8080)
[ -z "$REST_PORT" ] && die "Could not find an available port."

echo "Starting Ardulink REST service on port $REST_PORT..."
cd $SCRIPT_DIR/../target/ardulink/lib/
java -jar ardulink-rest-*.jar -port=$REST_PORT -connection "ardulink://serial?port=$DEVICE" &
JAVA_PID=$!
echo "Ardulink-REST started"
cd - >/dev/null

wait_for_port $REST_PORT 10 || die "Failed to detect Ardulink-REST server on port $REST_PORT."
echo "Ardulink-MQTT server is ready on port $MQTT_PORT."

# Step 5: Call the API endpoint and verify the response in the WebSocket container log file with a timeout
echo "Verifying WebSocket container response within 10 seconds..."
START_TIME=$(date +%s)
TIMEOUT=10

while true; do
    echo "Calling the API endpoint to set pin state..."
    RESPONSE=$(curl -s -X 'PUT' \
        "http://localhost:$REST_PORT/pin/digital/$PIN" \
        -H 'accept: application/json' \
        -H 'Content-Type: application/text' \
        -d 'true')
    if docker compose -f "$COMPOSE_FILE" logs websocat | jq -R -e 'split(" | ") | .[1] | fromjson? | select(.type == "pinState" and .pin == "'$PIN'" and .state == true)' >/dev/null 2>&1; then
        echo "Test passed. Received the expected WebSocket message."
        break
    fi

    CURRENT_TIME=$(date +%s)
    ELAPSED_TIME=$((CURRENT_TIME - START_TIME))
    [ $ELAPSED_TIME -ge $TIMEOUT ] && die "Test failed. Timeout reached without receiving the expected message."

    sleep 1
done

# If everything is successful, cleanup will be called automatically when the script exits
echo "Test completed successfully."
