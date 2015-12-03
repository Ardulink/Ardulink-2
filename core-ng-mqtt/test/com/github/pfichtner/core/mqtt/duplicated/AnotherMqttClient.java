package com.github.pfichtner.core.mqtt.duplicated;

import static com.github.pfichtner.ardulink.core.Pin.Type.ANALOG;
import static com.github.pfichtner.ardulink.core.Pin.Type.DIGITAL;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.junit.rules.ExternalResource;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.Type;

// TODO create a Mqtt test package and move AnotherMQttClient, ... to it
// TODO create a @MqttBroker Rule
public class AnotherMqttClient extends ExternalResource {

	private final MqttClient mqttClient;
	private final List<Message> messages = new CopyOnWriteArrayList<Message>();
	private final String topic;

	private static final Map<Type, String> typeMap = unmodifiableMap(typeMap());

	private static Map<Type, String> typeMap() {
		Map<Type, String> typeMap = new HashMap<Type, String>();
		typeMap.put(ANALOG, "A");
		typeMap.put(DIGITAL, "D");
		return typeMap;
	}

	public AnotherMqttClient(String topic) {
		this.topic = topic;
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

	protected static MqttClient mqttClient(String host, int port) {
		try {
			return new MqttClient("tcp://" + host + ":" + port,
					"anotherMqttClient");
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void before() throws Throwable {
		mqttClient.connect();
		mqttClient.subscribe("#");
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

	public void switchPin(Pin pin, Object value) throws MqttException,
			MqttPersistenceException {
		sendMessage(new Message(this.topic + typeMap.get(pin.getType())
				+ pin.pinNum() + "/set/value", String.valueOf(value)));
	}

	private void sendMessage(Message message) throws MqttException,
			MqttPersistenceException {
		this.mqttClient.publish(message.getTopic(), new MqttMessage(message
				.getMessage().getBytes()));
	}

	@Override
	protected void after() {
		if (this.mqttClient.isConnected()) {
			try {
				this.mqttClient.disconnect();
			} catch (MqttException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public List<Message> hasReceived() {
		return new ArrayList<Message>(messages);
	}

}
