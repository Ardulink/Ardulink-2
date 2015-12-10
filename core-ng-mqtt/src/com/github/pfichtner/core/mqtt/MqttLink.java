package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zu.ardulink.util.Integers;

import com.github.pfichtner.ardulink.core.AbstractListenerLink;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Tone;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.Pin.Type;
import com.github.pfichtner.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultDigitalPinValueChangedEvent;

public class MqttLink extends AbstractListenerLink {

	private static final Logger logger = LoggerFactory
			.getLogger(MqttLink.class);

	private static final String ANALOG = "A";
	private static final String DIGITAL = "D";
	private final String topic;
	private final Pattern mqttReceivePattern;
	private final MqttClient mqttClient;

	private static final Map<Type, String> typeMap = unmodifiableMap(typeMap());

	private static Map<Type, String> typeMap() {
		Map<Type, String> typeMap = new HashMap<Type, String>();
		typeMap.put(Type.ANALOG, ANALOG);
		typeMap.put(Type.DIGITAL, DIGITAL);
		return typeMap;
	}

	public MqttLink(MqttLinkConfig config) throws MqttException {
		checkArgument(config.getHost() != null, "host must not be null");
		checkArgument(config.getClientId() != null, "clientId must not be null");
		checkArgument(config.getTopic() != null, "topic must not be null");
		this.topic = config.getTopic();
		this.mqttReceivePattern = Pattern.compile(MqttLink.this.topic
				+ "([aAdD])(\\d+)/set/value");
		this.mqttClient = new MqttClient("tcp://" + config.getHost() + ":"
				+ config.getPort(), config.getClientId());
		listenToMqtt();
		this.mqttClient.setCallback(callback());
	}

	private void listenToMqtt() throws MqttSecurityException, MqttException {
		this.mqttClient.connect();
		subscribe();
	}

	public void subscribe() throws MqttException {
		this.mqttClient.subscribe(topic + '#');
	}

	private void unsubscribe() throws MqttException {
		this.mqttClient.unsubscribe(topic + '#');
	}

	private MqttCallback callback() {
		return new MqttCallback() {
			@Override
			public void messageArrived(String topic, MqttMessage mqttMessage)
					throws Exception {
				Matcher matcher = mqttReceivePattern.matcher(topic);
				if (matcher.matches() && matcher.groupCount() == 2) {
					Pin pin = pin(matcher.group(1),
							Integers.tryParse(matcher.group(2)));
					if (pin != null) {
						if (pin.is(Type.DIGITAL)) {
							fireStateChanged(new DefaultDigitalPinValueChangedEvent(
									(DigitalPin) pin,
									Boolean.parseBoolean(new String(mqttMessage
											.getPayload()))));
						} else if (pin.is(Type.ANALOG)) {
							fireStateChanged(new DefaultAnalogPinValueChangedEvent(
									(AnalogPin) pin,
									Integer.parseInt(new String(mqttMessage
											.getPayload()))));
						}
					}
				}
			}

			private Pin pin(String type, Integer pin) {
				if (pin != null) {
					if (DIGITAL.equalsIgnoreCase(type)) {
						return digitalPin(pin.intValue());
					} else if (ANALOG.equalsIgnoreCase(type)) {
						return analogPin(pin.intValue());
					}
				}
				return null;
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken deliveryToken) {
				// do nothing
			}

			@Override
			public void connectionLost(Throwable throwable) {
				fireConnectionLost();
				logger.warn("Connection to mqtt broker lost");
				do {
					try {
						SECONDS.sleep(1);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					try {
						logger.info("Trying to reconnect");
						listenToMqtt();
					} catch (Exception e) {
						logger.warn("Reconnect failed");
					}
				} while (!MqttLink.this.mqttClient.isConnected());
				logger.info("Successfully reconnected");
				fireRecconnected();
			}
		};
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

	private String getType(Pin pin) {
		return typeMap.get(pin.getType());
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		switchPin(ANALOG, analogPin, value);
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		switchPin(DIGITAL, digitalPin, value);
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
	public void sendTone(Tone tone) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendNoTone() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendCustomMessage(String message) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		try {
			if (this.mqttClient.isConnected()) {
				unsubscribe();
				// "kill" the callback since it retries to reconnect
				this.mqttClient.setCallback(null);
				this.mqttClient.disconnect();
			}
			this.mqttClient.close();
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}

}
