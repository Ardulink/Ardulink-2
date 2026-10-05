#!/bin/bash

# Systemtest of the REST application in the distribution. The protocol under test is
# given as the only argument, defaults to ardulink.
# Usage: systemtest-rest.sh [ardulink|firmata]

# Include the common script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/common.sh"

export COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

TEMP_DIR=$(mktemp -d)
export ARDULINK_DIR="$TEMP_DIR/firmware"
export VIRTUALDEVICE=$(find_first_unused_device "/dev/ttyUSB")
PIN="12"

resolve_protocol "${1:-ardulink}" "$VIRTUALDEVICE"
export FILENAME="$PROTO_FILENAME"
CONNECTION="$PROTO_CONNECTION"

trap cleanup EXIT INT TERM

export WS_PORT=$(find_unused_port 8000)
[ -z "$WS_PORT" ] && die "Could not find an available port."

# Step 1: Get the firmware of the protocol under test and place it in the firmware directory
install_firmware

# Step 2: Run the Docker container that emulates the Arduino
echo "Running Docker container for $FILENAME..."
export DEVICEUSER=$UID

docker compose -f "$COMPOSE_FILE" up -d virtualavr
wait_for_container_healthy virtualavr 120

# Step 3: Start websocat container (listening for messages sent by virtualavr)
docker compose -f "$COMPOSE_FILE" up -d websocat
echo "WebSocket container started"

# Let virtualavr report changes of pin $PIN. Firmata only sends pin reports when asked to,
# which is what makes the state written below visible on the WebSocket.
echo '{ "type": "pinMode", "pin": "'$PIN'", "mode": "digital" }' | docker compose -f "$COMPOSE_FILE" run --rm -T websocat-send-once "cat - | websocat ws://localhost:$WS_PORT"

# Step 4: Run the Java application in the background (detached mode)
REST_PORT=$(find_unused_port 8080)
[ -z "$REST_PORT" ] && die "Could not find an available port."

echo "Starting Ardulink REST service on port $REST_PORT..."
cd $SCRIPT_DIR/../target/ardulink/lib/
java -jar ardulink-rest-*.jar -port=$REST_PORT -connection "$CONNECTION" &
JAVA_PID=$!
echo "Ardulink-REST started"
cd - >/dev/null

wait_for_port $REST_PORT 10 || die "Failed to detect Ardulink-REST server on port $REST_PORT."
echo "Ardulink-REST server is ready on port $REST_PORT."

# Step 5: Switch the pin via the API endpoint and verify the pin state reached the
# emulated Arduino by checking the WebSocket container log file with a timeout
json_pattern=".type == \"pinState\" and .pin == \"$PIN\" and .state == true"
check_websocket_message \
    "curl -s -X 'PUT' 'http://localhost:$REST_PORT/pin/digital/$PIN' -H 'accept: application/json' -H 'Content-Type: application/text' -d 'true'" \
    "$json_pattern"

# If everything is successful, cleanup will be called automatically when the script exits
echo "Test completed successfully."
