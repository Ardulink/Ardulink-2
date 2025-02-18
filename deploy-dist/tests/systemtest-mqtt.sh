#!/bin/bash

# Include the common script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/common.sh"

export COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

TEMP_DIR=$(mktemp -d)
export ARDULINK_DIR="$TEMP_DIR/ArdulinkProtocol"
export VIRTUALDEVICE=$(find_first_unused_device "/dev/ttyUSB")
PIN="12"

trap cleanup EXIT INT TERM

export WS_PORT=$(find_unused_port 8000)
[ -z "$WS_PORT" ] && die "Could not find an available port."

# Step 1: Download the file and place it in the "ArdulinkProtocol" directory
echo "Downloading ArdulinkProtocol.ino..."
mkdir -p "$ARDULINK_DIR"
wget -qO "$ARDULINK_DIR/ArdulinkProtocol.ino.hex" https://github.com/Ardulink/Firmware/releases/download/v1.2.0/ArdulinkProtocol.ino.hex

# Step 2: Run the Docker container that emulates the Arduino
echo "Running Docker container for ArdulinkProtocol..."
export DEVICEUSER=$UID
docker compose -f "$COMPOSE_FILE" up -d virtualavr
wait_for_container_healthy virtualavr

# Step 3: Start websocat container (listening for messages sent by virtualavr)
docker compose -f "$COMPOSE_FILE" up -d websocat
echo "WebSocket container started"

# Enable listening on pin $PIN
echo '{ "type": "pinMode", "pin": "'$PIN'", "mode": "digital" }' | docker compose -f "$COMPOSE_FILE" run --rm -T websocat-send-once "cat - | websocat ws://localhost:$WS_PORT"

# Step 4: Run the Java application in the background (detached mode)
export MQTT_PORT=$(find_unused_port 1883)
[ -z "$MQTT_PORT" ] && die "Could not find an available port."

echo "Starting Ardulink MQTT service on port $MQTT_PORT..."
cd $SCRIPT_DIR/../target/ardulink/lib/
java -jar ardulink-mqtt-*.jar -standalone -brokerPort=$MQTT_PORT -connection "ardulink://serial?port=$VIRTUALDEVICE" &
JAVA_PID=$!
echo "Ardulink-MQTT started"
cd - >/dev/null

wait_for_port $MQTT_PORT 10 || die "Failed to detect Ardulink-MQTT server on port $MQTT_PORT."
echo "Ardulink-MQTT server is ready on port $MQTT_PORT."

# Step 5: Publish MQTT message and verify the response in the WebSocket container log file with a timeout
echo "Verifying WebSocket container response within 10 seconds..."
START_TIME=$(date +%s)
TIMEOUT=10

# Step 6: Define the MQTT topic and message dynamically
export MQTT_HOST="localhost"
export MQTT_TOPIC="home/devices/ardulink/D$PIN"
export MQTT_MESSAGE="true"

json_pattern=".type == \"pinState\" and .pin == \"$PIN\" and .state == true"
check_websocket_message \
    "docker compose run --rm mqtt-pub-once" \
    "$json_pattern"

# If everything is successful, cleanup will be called automatically when the script exits
echo "Test completed successfully."
