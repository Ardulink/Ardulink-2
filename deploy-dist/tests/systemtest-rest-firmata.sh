#!/bin/bash

# Include the common script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/common.sh"

export COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

TEMP_DIR=$(mktemp -d)
export ARDULINK_DIR="$TEMP_DIR/StandardFirmata"
export FILENAME="StandardFirmata.hex"
export VIRTUALDEVICE=$(find_first_unused_device "/dev/ttyUSB")
PIN="12"

# proto=Firmata is only shipped next to the applications in the distribution, not on their
# classpath. Selecting it therefore proves that a protocol is discovered from the module
# directory (ardulink.module.dir, defaulting to the working directory) next to the jar.
CONNECTION="ardulink://serial?port=$VIRTUALDEVICE&baudrate=9600&proto=Firmata&pingprobe=true&waitsecs=10"

trap cleanup EXIT INT TERM

export WS_PORT=$(find_unused_port 8000)
[ -z "$WS_PORT" ] && die "Could not find an available port."

# Step 1: Take the firmware of the integration tests and place it in the firmware directory
echo "Preparing $FILENAME..."
FIRMWARE="$SCRIPT_DIR/../../ardulink-core-base/src/test/resources/firmware/$FILENAME"
[ -f "$FIRMWARE" ] || die "Firmware not found: $FIRMWARE"
mkdir -p "$ARDULINK_DIR"
cp "$FIRMWARE" "$ARDULINK_DIR/$FILENAME"

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
echo "Verifying WebSocket container response within 10 seconds..."
START_TIME=$(date +%s)
TIMEOUT=10

json_pattern=".type == \"pinState\" and .pin == \"$PIN\" and .state == true"
check_websocket_message \
    "curl -s -X 'PUT' 'http://localhost:$REST_PORT/pin/digital/$PIN' -H 'accept: application/json' -H 'Content-Type: application/text' -d 'true'" \
    "$json_pattern"

# If everything is successful, cleanup will be called automatically when the script exits
echo "Test completed successfully."