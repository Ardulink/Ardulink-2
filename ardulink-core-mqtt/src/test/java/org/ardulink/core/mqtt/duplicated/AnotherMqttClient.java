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
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.Pin;
import org.ardulink.core.Pin.Type;
import org.ardulink.util.MapBuilder;
import org.ardulink.util.Throwables;
import org.assertj.core.api.ThrowingConsumer;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
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

	private final MqttClient mqttClient;
	private final List<Message> messages = new CopyOnWriteArrayList<>();
	private final List<Message> messagesView = unmodifiableList(messages);
	private final String topic;

	private static final Map<Type, String> typeMap = unmodifiableMap(MapBuilder.<Type, String>newMapBuilder() //
			.put(ANALOG, "A") //
			.put(DIGITAL, "D") //
			.build());

	private static final int QOS_LEVEL = 1;

	private boolean appendValueGet;

	public static AnotherMqttClient newClient(String topic, int port) {
		try {
			return new AnotherMqttClient(topic, port);
		} catch (MqttException e) {
			throw Throwables.propagate(e);
		}
	}

	private AnotherMqttClient(String topic, int port) throws MqttException {
		this.topic = topic.endsWith("/") ? topic : topic + "/";
		this.mqttClient = mqttClient("localhost", port);
	}

	public AnotherMqttClient appendValueSet(boolean appendValueGet) {
		this.appendValueGet = appendValueGet;
		return this;
	}

	protected static MqttClient mqttClient(String host, int port) throws MqttException {
		return new MqttClient(serverUrl(host, port), clientId(), new MemoryPersistence());
	}

	private static String serverUrl(String host, int port) {
		return "tcp://" + host + ":" + port;
	}

	private static String clientId() {
		return "amc-" + Thread.currentThread().getId() + "-" + System.currentTimeMillis();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws MqttSecurityException, MqttException {
		connect();
	}

	public AnotherMqttClient connect() throws MqttSecurityException, MqttException {
		this.mqttClient.setCallback(resubsriber());
		this.mqttClient.connect(options());
		subscribe();
		return this;
	}

	private MqttCallbackExtended resubsriber() {
		return new MqttCallbackExtended() {

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				// noop
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				// noop
			}

			@Override
			public void connectionLost(Throwable cause) {
				// noop
			}

			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				try {
					subscribe();
				} catch (MqttException e) {
					Throwables.propagate(e);
				}
			}
		};
	}

	private void subscribe() throws MqttException {
		this.mqttClient.subscribe("#", QOS_LEVEL, (t, m) -> messages.add(new Message(t, new String(m.getPayload()))));
	}

	private static MqttConnectOptions options() {
		MqttConnectOptions options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(10);
		return options;
	}

	public void awaitMessages(ThrowingConsumer<List<Message>> throwingConsumer) {
		await().timeout(ofSeconds(10)).pollInterval(ofMillis(100)).untilAsserted(() -> {
			throwingConsumer.accept(messagesView);
			messages.clear();
		});
	}

	public void switchPin(Pin pin, Object value) throws IOException {
		sendMessage(new Message(append(this.topic + typeMap.get(pin.getType()) + pin.pinNum()), String.valueOf(value)));
	}

	private String append(String message) {
		return appendValueGet ? message + "/value/get" : message;
	}

	private void sendMessage(Message message) throws IOException {
		try {
			mqttClient.publish(message.getTopic(), message.getMessage().getBytes(), QOS_LEVEL, false);
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws MqttException {
		close();
	}

	public boolean isConnected() {
		return this.mqttClient.isConnected();
	}

	public void close() throws MqttException {
		if (this.mqttClient.isConnected()) {
			this.mqttClient.unsubscribe("#");
			this.mqttClient.disconnect();
		}
	}

	public List<Message> getReceived() {
		return messagesView;
	}

}
