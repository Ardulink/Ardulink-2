package com.github.pfichtner.ardulink.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class AnotherMqttClient {

	private final String topic;
	private final MqttClient mqttClient;
	private final List<Message> messages = new ArrayList<Message>();

	public AnotherMqttClient(String topic) throws MqttSecurityException,
			MqttException {
		this.topic = topic;
		this.mqttClient = mqttClient();
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

	private static MqttClient mqttClient() throws MqttException,
			MqttSecurityException {
		return new MqttClient("tcp://localhost:1883", "anotherMqttClient");
	}

	public AnotherMqttClient connect() throws MqttSecurityException,
			MqttException {
		mqttClient.connect();
		mqttClient.subscribe("#");
		return this;
	}

	public void switchDigitalPin(int pin, Object value)
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
		return MqttMessageBuilder.mqttMessageWithBasicTopic(topic);
	}

	private void sendMessage(Message msg) throws MqttException,
			MqttPersistenceException {
		mqttClient.publish(msg.getTopic(), new MqttMessage(msg.getMessage()
				.getBytes()));
	}

	public void disconnect() throws MqttException {
		if (this.mqttClient.isConnected()) {
			this.mqttClient.disconnect();
		}
	}

	public List<Message> hasReceived() {
		return new ArrayList<Message>(messages);
	}

}
