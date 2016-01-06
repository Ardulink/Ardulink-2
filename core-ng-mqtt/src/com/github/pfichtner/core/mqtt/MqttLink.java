package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.unmodifiableMap;
import static org.fusesource.mqtt.client.QoS.AT_LEAST_ONCE;
import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.Topic;
import org.zu.ardulink.util.Integers;

import com.github.pfichtner.ardulink.core.AbstractListenerLink;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.Pin.Type;
import com.github.pfichtner.ardulink.core.Tone;
import com.github.pfichtner.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultDigitalPinValueChangedEvent;

public class MqttLink extends AbstractListenerLink {

	private static final String ANALOG = "A";
	private static final String DIGITAL = "D";
	private final String topic;
	private final Pattern mqttReceivePattern;
	private final MQTT mqttClient;
	private FutureConnection connection;

	private static final Map<Type, String> typeMap = unmodifiableMap(typeMap());

	private static Map<Type, String> typeMap() {
		Map<Type, String> typeMap = new HashMap<Type, String>();
		typeMap.put(Type.ANALOG, ANALOG);
		typeMap.put(Type.DIGITAL, DIGITAL);
		return typeMap;
	}

	public MqttLink(MqttLinkConfig config) throws IOException {
		checkArgument(config.getHost() != null, "host must not be null");
		checkArgument(config.getClientId() != null, "clientId must not be null");
		checkArgument(config.getTopic() != null, "topic must not be null");
		this.topic = config.getTopic();
		this.mqttReceivePattern = Pattern.compile(MqttLink.this.topic
				+ "([aAdD])(\\d+)\\/value\\/set");
		this.mqttClient = newClient(config);
		this.connection = new FutureConnection(new CallbackConnection(new MQTT(
				this.mqttClient)) {

			@Override
			public CallbackConnection listener(Listener listener) {
				return super.listener(multiplex(listener, l()));
			}

			private Listener multiplex(final Listener... listeners) {
				return new Listener() {

					@Override
					public void onPublish(UTF8Buffer topic, Buffer body,
							Runnable ack) {
						for (Listener listener : listeners) {
							listener.onPublish(topic, body, ack);
						}
					}

					@Override
					public void onFailure(Throwable value) {
						for (Listener listener : listeners) {
							listener.onFailure(value);
						}
					}

					@Override
					public void onDisconnected() {
						for (Listener listener : listeners) {
							listener.onDisconnected();
						}
					}

					@Override
					public void onConnected() {
						for (Listener listener : listeners) {
							listener.onConnected();
						}
					}
				};
			}
		});
		new Thread() {
			public void run() {
				while (true) {
					try {
						Message message = exec(connection.receive());
						Matcher matcher = mqttReceivePattern.matcher(message
								.getTopic());
						if (matcher.matches() && matcher.groupCount() == 2) {
							Pin pin = pin(matcher.group(1),
									Integers.tryParse(matcher.group(2)));
							if (pin != null) {
								if (pin.is(Type.DIGITAL)) {
									fireStateChanged(new DefaultDigitalPinValueChangedEvent(
											(DigitalPin) pin,
											Boolean.parseBoolean(new String(
													message.getPayload()))));
								} else if (pin.is(Type.ANALOG)) {
									fireStateChanged(new DefaultAnalogPinValueChangedEvent(
											(AnalogPin) pin,
											Integer.parseInt(new String(message
													.getPayload()))));
								}
							}
						}
						message.ack();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			};

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

		}.start();
		exec(connection.connect());
		subscribe();
	}

	private Listener l() {
		return new Listener() {

			@Override
			public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
				// nothing to do
			}

			@Override
			public void onFailure(Throwable value) {
				// nothing to do
			}

			@Override
			public void onDisconnected() {
				fireConnectionLost();
			}

			@Override
			public void onConnected() {
				fireRecconnected();
			}
		};
	}

	private MQTT newClient(MqttLinkConfig config) {
		MQTT client = new MQTT();
		client.setClientId(config.getClientId());
		try {
			client.setHost("tcp://" + config.getHost() + ":" + config.getPort());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return client;

	}

	private void subscribe() {
		try {
			exec(connection.subscribe(new Topic[] { new Topic(topic + "#",
					AT_LEAST_ONCE) }));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
				+ "/value/set";
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
		publish(topic + type + pin.pinNum() + "/value/set", value);
	}

	private void publish(final String topic, Object value) throws IOException {
		exec(connection.publish(topic, String.valueOf(value).getBytes(),
				AT_LEAST_ONCE, false));
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
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		deregisterAllEventListeners();
		if (this.connection.isConnected()) {
			exec(this.connection.disconnect());
		}
		super.close();
	}

	private static <T> T exec(Future<T> future) throws IOException {
		try {
			return future.await();
		} catch (Exception e) {
			throw new IOException();
		}
	}

}
