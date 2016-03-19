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
package org.ardulink.mqtt.util;

import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.fusesource.mqtt.client.QoS.AT_LEAST_ONCE;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Topic;

import org.ardulink.core.Pin;
import org.ardulink.core.Pin.Type;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class AnotherMqttClient implements Closeable {

	public static class Builder {

		private String host = "localhost";
		private int port = 1883;
		private String topic;
		public String clientId = "anotherMqttClient";

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder topic(String topic) {
			this.topic = topic;
			return this;
		}

		public Builder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public AnotherMqttClient connect() {
			try {
				AnotherMqttClient client = new AnotherMqttClient(this);
				client.connect();
				return client;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	private final MQTT mqttClient;
	private FutureConnection connection;
	private final List<Message> messages = new CopyOnWriteArrayList<Message>();
	private final String topic;

	private static final Map<Type, String> typeMap = unmodifiableMap(typeMap());

	private static Map<Type, String> typeMap() {
		Map<Type, String> typeMap = new HashMap<Type, String>();
		typeMap.put(ANALOG, "A");
		typeMap.put(DIGITAL, "D");
		return typeMap;
	}

	private AnotherMqttClient(Builder builder) {
		this.topic = builder.topic.endsWith("/") ? builder.topic
				: builder.topic + "/";
		this.mqttClient = mqttClient(builder.host, builder.port);
	}

	protected static MQTT mqttClient(String host, int port) {
		try {
			MQTT client = new MQTT();
			client.setCleanSession(true);
			client.setClientId("amc-" + Thread.currentThread().getId() + "-"
					+ System.currentTimeMillis());
			client.setHost("tcp://" + host + ":" + port);
			return client;
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public AnotherMqttClient connect() throws IOException {
		connection = mqttClient.futureConnection();
		exec(connection.connect());
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						org.fusesource.mqtt.client.Message message = exec(connection
								.receive());
						messages.add(new Message(message.getTopic(),
								new String(message.getPayload())));
						message.ack();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		exec(connection
				.subscribe(new Topic[] { new Topic("#", AT_LEAST_ONCE) }));
		return this;
	}

	public List<Message> getMessages() {
		try {
			MILLISECONDS.sleep(25);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return new ArrayList<Message>(this.messages);
	}

	public List<Message> pollMessages() {
		List<Message> messages = getMessages();
		this.messages.clear();
		return messages;
	}

	public void switchPin(Pin pin, Object value) throws IOException {
		sendMessage(new Message(this.topic + typeMap.get(pin.getType())
				+ pin.pinNum() + "/value/set", String.valueOf(value)));
	}

	private void sendMessage(final Message message) throws IOException {
		exec(connection.publish(message.getTopic(), message.getMessage()
				.getBytes(), AT_LEAST_ONCE, false));
	}

	@Override
	public void close() throws IOException {
		if (this.connection.isConnected()) {
			exec(connection.unsubscribe(new String[] { new String("#") }));
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
		return new ArrayList<Message>(messages);
	}

}
