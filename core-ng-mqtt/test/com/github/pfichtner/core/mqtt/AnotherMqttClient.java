package com.github.pfichtner.core.mqtt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class AnotherMqttClient {

	private final MqttClient mqttClient;
	private final List<Message> messages = new ArrayList<Message>();

	public AnotherMqttClient(String topic) throws MqttSecurityException,
			MqttException {
		this.mqttClient = mqttClient("localhost", 1883);
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

	protected static MqttClient mqttClient(String host, int port)
			throws MqttException {
		return new MqttClient("tcp://" + host + ":" + port, "anotherMqttClient");
	}

	public AnotherMqttClient connect() throws MqttSecurityException,
			MqttException {
		mqttClient.connect();
		mqttClient.subscribe("#");
		return this;
	}

	public List<Message> getMessages() {
		return new ArrayList<Message>(this.messages);
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
