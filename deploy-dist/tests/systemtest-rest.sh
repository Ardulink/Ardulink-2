#!/bin/bash

# Systemtest of the REST application in the distribution. The protocol under test is
# given as the only argument, defaults to ardulink.
# Usage: systemtest-rest.sh [ardulink|firmata|telemetrix]

# Include the common script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/common.sh"

export COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

TEMP_DIR=$(mktemp -d)
export ARDULINK_DIR="$TEMP_DIR/firmware"
DIGITAL_PIN="12"
ANALOG_WRITE_PIN="10"
ANALOG_WRITE_VALUE="42"
ANALOG_READ_PIN="2"
ANALOG_READ_VALUE="456"

resolve_protocol "${1:-ardulink}"
export FILENAME="$PROTO_FILENAME"

trap cleanup EXIT INT TERM

# Step 1: Get the firmware of the protocol under test and place it in the firmware directory
install_firmware

# Step 1b: Optional protocols must be enabled (moved into lib/) before the application starts
enable_optional_protocol

# Step 2: Run the Docker container that emulates the Arduino
echo "Running Docker container for $FILENAME..."
export DEVICEUSER=$UID

with_port_lock start_virtualavr
CONNECTION=$(proto_connection "$VIRTUALDEVICE")
wait_for_container_healthy virtualavr 120

# Step 3: Start websocat container (listening for messages sent by virtualavr)
docker compose -p "$STACK_ID" -f "$COMPOSE_FILE" up -d websocat
echo "WebSocket container started"

# Let virtualavr report changes of the pins. Firmata only sends pin reports when asked to,
# which is what makes the states written below visible on the WebSocket.
for pin_mode in \
    '{ "type": "pinMode", "pin": "'$DIGITAL_PIN'", "mode": "digital" }' \
    '{ "type": "pinMode", "pin": "'$ANALOG_WRITE_PIN'", "mode": "analog" }' \
    '{ "type": "pinMode", "pin": "A'$ANALOG_READ_PIN'", "mode": "analog" }'; do
    echo "$pin_mode" | docker compose -p "$STACK_ID" -f "$COMPOSE_FILE" run --rm -T websocat-send-once "cat - | websocat ws://localhost:$WS_PORT"
done

# Step 4: Run the Java application in the background (detached mode)
start_rest_service() {
    REST_PORT=$(find_unused_port 8080)
    [ -z "$REST_PORT" ] && die "Could not find an available port."

    echo "Starting Ardulink REST service on port $REST_PORT..."
    cd $SCRIPT_DIR/../target/ardulink/lib/
    java -jar ardulink-rest-*.jar -port=$REST_PORT -connection "$CONNECTION" 9>&- &
    JAVA_PID=$!
    echo "Ardulink-REST started"
    cd - >/dev/null

    wait_for_port $REST_PORT 10 || die "Failed to detect Ardulink-REST server on port $REST_PORT."
    kill -0 "$JAVA_PID" 2>/dev/null || die "Ardulink-REST process exited, port $REST_PORT was taken."
}

# Picking the port and binding it happens under the same lock, so a parallel
# run cannot pick the same port in between.
with_port_lock start_rest_service
echo "Ardulink-REST server is ready on port $REST_PORT."

# Step 5: Switch the pin via the API endpoint and verify the pin state reached the
# emulated Arduino by checking the WebSocket container log file with a timeout
check_pin_write() {
    local type="$1" pin="$2" ws_pin="$3" value="$4"
    local json_pattern=".type == \"pinState\" and .pin == \"$ws_pin\" and .state == $value"
    check_websocket_message \
        "curl -s -X 'PUT' 'http://localhost:$REST_PORT/pin/$type/$pin' -H 'accept: application/json' -H 'Content-Type: application/text' -d '$value'" \
        "$json_pattern"
}

# Step 6: Make the firmware report the unchanged state, then query the pin twice.
# The reads must return the last known state, so a read neither waits for nor
# consumes a new pin event.
check_pin_read() {
    local type="$1" pin="$2" ws_pin="$3" value="$4"
    curl -s -X 'PATCH' "http://localhost:$REST_PORT/pin/$type/$pin" -H 'Content-Type: application/text' -d 'listen=true' && echo
    echo '{ "type": "pinState", "pin": "'$ws_pin'", "state": '$value' }' | docker compose -p "$STACK_ID" -f "$COMPOSE_FILE" run --rm -T websocat-send-once "cat - | websocat ws://localhost:$WS_PORT"

    local response
    for query in 1 2; do
        for attempt in $(seq 1 10); do
            response=$(curl -s -X 'GET' "http://localhost:$REST_PORT/pin/$type/$pin" -H 'accept: application/json')
            [ "$response" = "$value" ] && break
            sleep 1
        done
        [ "$response" = "$value" ] || die "GET $query on $type pin $pin returned '$response' instead of '$value'."
    done
    echo "Both GET queries returned the unchanged $type pin state."
}

check_pin_write digital "$DIGITAL_PIN" "$DIGITAL_PIN" true
check_pin_read digital "$DIGITAL_PIN" "$DIGITAL_PIN" true

check_pin_write analog "$ANALOG_WRITE_PIN" "$ANALOG_WRITE_PIN" "$ANALOG_WRITE_VALUE"
check_pin_read analog "$ANALOG_READ_PIN" "A$ANALOG_READ_PIN" "$ANALOG_READ_VALUE"

# If everything is successful, cleanup will be called automatically when the script exits
echo "Test completed successfully."
