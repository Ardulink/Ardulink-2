package com.github.pfichtner.core.mqtt.duplicated;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

// TODO create a Mqtt test package and move AnotherMQttClient, ... to it
// TODO create a @MqttBroker Rule
public class AnotherMqttClient {

	private final MqttClient mqttClient;
	private final List<Message> messages = new CopyOnWriteArrayList<Message>();

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
		try {
			MILLISECONDS.sleep(25);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return new ArrayList<Message>(this.messages);
	}

	public List<Message> pollMessages() {
		List<Message> messages = new ArrayList<Message>(this.messages);
		this.messages.clear();
		return messages;
	}

	public void sendMessage(Message message) throws MqttException,
			MqttPersistenceException {
		this.mqttClient.publish(message.getTopic(), new MqttMessage(message
				.getMessage().getBytes()));
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
