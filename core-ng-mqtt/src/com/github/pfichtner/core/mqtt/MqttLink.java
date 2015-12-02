package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pins.isAnalog;
import static com.github.pfichtner.ardulink.core.Pins.isDigital;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.github.pfichtner.ardulink.core.AbstractListenerLink;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;

public class MqttLink extends AbstractListenerLink {

	// TODO subscribe to # add call stateChange

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
	public void startListening(Pin pin) throws IOException {
		publish(controlTopic(pin), TRUE);
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		publish(controlTopic(pin), FALSE);
	}

	private String controlTopic(Pin pin) {
		return topic + "system/listening/" + getType(pin) + pin.pinNum()
				+ "/set/value";
	}

	private static String getType(Pin pin) {
		if (isAnalog(pin)) {
			return "A";
		} else if (isDigital(pin)) {
			return "D";
		} else {
			throw new IllegalStateException("Pin " + pin
					+ " is not digital nor analog");
		}
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
		publish(topic + type + pin.pinNum() + "/set/value", value);
	}

	private void publish(String topic, Object value) throws IOException {
		try {
			this.mqttClient.publish(topic, new MqttMessage(String
					.valueOf(value).getBytes()));
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
