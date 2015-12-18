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
package com.github.pfichtner.ardulink.util;

import static com.github.pfichtner.ardulink.util.MqttMessageBuilder.mqttMessageWithBasicTopic;
import static java.lang.String.format;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 *         [adsense]
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
			AnotherMqttClient client = new AnotherMqttClient(this);
			client.connect();
			return client;
		}

	}

	private final String topic;
	private final MqttClient mqttClient;
	private final List<Message> messages = new ArrayList<Message>();

	public static Builder builder() {
		return new Builder();
	}

	public AnotherMqttClient(Builder builder) {
		this.topic = builder.topic;
		this.mqttClient = mqttClient(builder);
		this.mqttClient.setCallback(new MqttCallback() {

			@Override
			public void messageArrived(String topic, MqttMessage message)
					throws Exception {
				messages.add(new Message(topic,
						new String(message.getPayload())));
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken deliveryToken) {
			}

			@Override
			public void connectionLost(Throwable throwable) {
			}

		});
	}

	private static MqttClient mqttClient(Builder builder) {
		try {
			return new MqttClient(format("tcp://%s:%s", builder.host,
					builder.port), builder.clientId);
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	public AnotherMqttClient connect() {
		try {
			mqttClient.connect();
			mqttClient.subscribe("#");
			return this;
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	public void switchDigitalPin(int pin, boolean value)
			throws MqttPersistenceException, MqttException {
		sendMessage(createSetMessage(newMsgBuilder().digitalPin(pin), value));
	}

	public void switchAnalogPin(int pin, Object value)
			throws MqttPersistenceException, MqttException {
		sendMessage(createSetMessage(newMsgBuilder().analogPin(pin), value));
	}

	private Message createSetMessage(MqttMessageBuilder msgBuilder, Object value) {
		return msgBuilder.setValue(value);
	}

	private MqttMessageBuilder newMsgBuilder() {
		return mqttMessageWithBasicTopic(topic);
	}

	private void sendMessage(Message msg) throws MqttException,
			MqttPersistenceException {
		mqttClient.publish(msg.getTopic(), new MqttMessage(msg.getMessage()
				.getBytes()));
	}

	@Override
	public void close() throws IOException {
		try {
			if (this.mqttClient.isConnected()) {
				this.mqttClient.disconnect();
			}
			this.mqttClient.close();
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Message> hasReceived() {
		return new ArrayList<Message>(messages);
	}

}
