/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.core.mqtt.duplicated;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.unmodifiableMap;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.fusesource.mqtt.client.QoS.AT_LEAST_ONCE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.ardulink.core.Pin;
import org.ardulink.core.Pin.Type;
import org.ardulink.util.URIs;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Topic;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

// TODO create a Mqtt test package and move AnotherMQttClient, ... to it
// TODO create a @MqttBroker Extension
/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class AnotherMqttClient implements BeforeEachCallback, AfterEachCallback {

	private final MQTT mqttClient;
	private FutureConnection connection;
	private final List<Message> messages = new CopyOnWriteArrayList<>();
	private final String topic;

	private static final Map<Type, String> typeMap = unmodifiableMap(typeMap());
	private boolean appendValueGet;

	private static Map<Type, String> typeMap() {
		Map<Type, String> typeMap = new HashMap<>();
		typeMap.put(ANALOG, "A");
		typeMap.put(DIGITAL, "D");
		return typeMap;
	}

	public static AnotherMqttClient newClient(String topic, int port) {
		return new AnotherMqttClient(topic, port);
	}

	private AnotherMqttClient(String topic, int port) {
		this.topic = topic.endsWith("/") ? topic : topic + "/";
		this.mqttClient = mqttClient("localhost", port);
	}

	public AnotherMqttClient appendValueSet(boolean appendValueGet) {
		this.appendValueGet = appendValueGet;
		return this;
	}

	protected static MQTT mqttClient(String host, int port) {
		MQTT client = new MQTT();
		client.setCleanSession(true);
		client.setClientId("amc-" + Thread.currentThread().getId() + "-" + System.currentTimeMillis());
		client.setHost(URIs.newURI("tcp://" + host + ":" + port));
		return client;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws IOException {
		connect();
	}

	public AnotherMqttClient connect() throws IOException {
		connection = mqttClient.futureConnection();
		exec(connection.connect());
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						org.fusesource.mqtt.client.Message message = exec(connection.receive());
						messages.add(new Message(message.getTopic(), new String(message.getPayload())));
						message.ack();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		exec(connection.subscribe(new Topic[] { new Topic("#", AT_LEAST_ONCE) }));
		return this;
	}

	public void awaitMessages(Predicate<? super List<? extends Message>> predicate) {
		await().timeout(ofSeconds(10)).pollInterval(ofMillis(100))
				.untilAsserted(() -> assertThat(messages).matches(predicate));
	}

	public void clear() {
		this.messages.clear();
	}

	public void switchPin(Pin pin, Object value) throws IOException {
		sendMessage(new Message(append(this.topic + typeMap.get(pin.getType()) + pin.pinNum()), String.valueOf(value)));
	}

	private String append(String message) {
		return appendValueGet ? message + "/value/get" : message;
	}

	private void sendMessage(Message message) throws IOException {
		exec(connection.publish(message.getTopic(), message.getMessage().getBytes(), AT_LEAST_ONCE, false));
	}

	@Override
	public void afterEach(ExtensionContext context) throws IOException {
		close();
	}

	public void close() throws IOException {
		if (this.connection.isConnected()) {
			exec(connection.unsubscribe(new String[] { "#" }));
			exec(this.connection.disconnect());
		}
	}

	private static <T> T exec(Future<T> future) throws IOException {
		try {
			return future.await();
		} catch (Exception e) {
			throw new IOException();
		}
	}

	public List<Message> hasReceived() {
		return new ArrayList<>(messages);
	}

}
