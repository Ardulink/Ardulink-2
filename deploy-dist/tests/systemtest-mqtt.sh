#!/bin/bash

# Systemtest of the MQTT application in the distribution. The protocol under test is
# given as the only argument, defaults to ardulink.
# Usage: systemtest-mqtt.sh [ardulink|firmata]

# Include the common script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/common.sh"

export COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

TEMP_DIR=$(mktemp -d)
export ARDULINK_DIR="$TEMP_DIR/firmware"
PIN="12"

resolve_protocol "${1:-ardulink}"
export FILENAME="$PROTO_FILENAME"

trap cleanup EXIT INT TERM

# Step 1: Get the firmware of the protocol under test and place it in the firmware directory
install_firmware

# Step 2: Run the Docker container that emulates the Arduino
echo "Running Docker container for $FILENAME..."
export DEVICEUSER=$UID
with_port_lock start_virtualavr
CONNECTION=$(proto_connection "$VIRTUALDEVICE")
wait_for_container_healthy virtualavr 120

# Step 3: Start websocat container (listening for messages sent by virtualavr)
docker compose -p "$STACK_ID" -f "$COMPOSE_FILE" up -d websocat
echo "WebSocket container started"

# Let virtualavr report changes of pin $PIN. Firmata only sends pin reports when asked to,
# which is what makes the state published below visible on the WebSocket.
echo '{ "type": "pinMode", "pin": "'$PIN'", "mode": "digital" }' | docker compose -p "$STACK_ID" -f "$COMPOSE_FILE" run --rm -T websocat-send-once "cat - | websocat ws://localhost:$WS_PORT"

# Step 4: Run the Java application in the background (detached mode)
start_mqtt_service() {
    export MQTT_PORT=$(find_unused_port 1883)
    [ -z "$MQTT_PORT" ] && die "Could not find an available port."

    echo "Starting Ardulink MQTT service on port $MQTT_PORT..."
    cd $SCRIPT_DIR/../target/ardulink/lib/
    java -jar ardulink-mqtt-*.jar -standalone -brokerPort=$MQTT_PORT -connection "$CONNECTION" 9>&- &
    JAVA_PID=$!
    echo "Ardulink-MQTT started"
    cd - >/dev/null

    wait_for_port $MQTT_PORT 10 || die "Failed to detect Ardulink-MQTT server on port $MQTT_PORT."
    kill -0 "$JAVA_PID" 2>/dev/null || die "Ardulink-MQTT process exited, port $MQTT_PORT was taken."
}

# Picking the port and binding it happens under the same lock, so a parallel
# run cannot pick the same port in between.
with_port_lock start_mqtt_service
echo "Ardulink-MQTT server is ready on port $MQTT_PORT."

# Step 5: Publish MQTT message and verify the response in the WebSocket container log file with a timeout
export MQTT_HOST="localhost"
export MQTT_TOPIC="home/devices/ardulink/D$PIN"
export MQTT_MESSAGE="true"

json_pattern=".type == \"pinState\" and .pin == \"$PIN\" and .state == true"
check_websocket_message \
    "docker compose -p "$STACK_ID" -f "$COMPOSE_FILE" run --rm mqtt-pub-once" \
    "$json_pattern"

# If everything is successful, cleanup will be called automatically when the script exits
echo "Test completed successfully."
