package com.github.pfichtner.core.mqtt;

import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.events.EventListener;

public class MqttLink implements Link {

	private final String topic;
	private final MqttClient mqttClient;

	public MqttLink(MqttLinkConfig config) throws MqttException {
		checkArgument(config.getHost() != null, "host must not be null");
		checkArgument(config.getClientId() != null, "clientId must not be null");
		checkArgument(config.getTopic() != null, "topic must not be null");
		this.topic = config.getTopic();
		this.mqttClient = new MqttClient("tcp://" + config.getHost() + ":"
				+ config.getPort(), config.getClientId());
		this.mqttClient.connect();
	}

	@Override
	public Link addListener(EventListener listener) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Link removeListener(EventListener listener) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void startListening(Pin pin) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		switchPin("A", analogPin, value);
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		switchPin("D", digitalPin, value);
	}

	private void switchPin(String type, Pin pin, Object value)
			throws IOException {
		try {
			this.mqttClient.publish(topic + type + pin.pinNum() + "/set/value",
					new MqttMessage(String.valueOf(value).getBytes()));
		} catch (MqttPersistenceException e) {
			throw new IOException(e);
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		try {
			this.mqttClient.disconnect();
			this.mqttClient.close();
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}
}
