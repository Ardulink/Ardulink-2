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

package org.ardulink.core.mqtt;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.unmodifiableMap;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Throwables.propagate;
import static org.fusesource.mqtt.client.QoS.AT_LEAST_ONCE;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Pin.Type;
import org.ardulink.core.Tone;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import org.ardulink.util.URIs;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttLink extends AbstractListenerLink {

	private static final Logger log = LoggerFactory.getLogger(MqttLink.class);

	private static final String ANALOG = "A";
	private static final String DIGITAL = "D";
	private final String topic;
	private final Pattern mqttReceivePattern;
	private final MQTT mqttClient;
	private BlockingConnection connection;

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
		this.connection = new BlockingConnection(futureConnection());
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Message message = connection.receive();
						Matcher matcher = mqttReceivePattern.matcher(message
								.getTopic());
						if (matcher.matches() && matcher.groupCount() == 2) {
							Pin pin = pin(matcher.group(1),
									tryParse(matcher.group(2)));
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
						log.error("Error while waiting for new message", e);
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
		try {
			connection.connect();
			subscribe();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private FutureConnection futureConnection() {
		return new FutureConnection(callbackConnection());
	}

	private CallbackConnection callbackConnection() {
		return new CallbackConnection(new MQTT(this.mqttClient)) {

			@Override
			public CallbackConnection listener(Listener listener) {
				return super
						.listener(multiplex(listener, connectionListener()));
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
		};
	}

	private Listener connectionListener() {
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
				fireReconnected();
			}
		};
	}

	private MQTT newClient(MqttLinkConfig config) {
		MQTT client = new MQTT();
		client.setClientId(config.getClientId());
		client.setHost(URIs.newURI("tcp://" + config.getHost() + ":"
				+ config.getPort()));
		return client;

	}

	private void subscribe() throws Exception {
		connection
				.subscribe(new Topic[] { new Topic(topic + "#", AT_LEAST_ONCE) });
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
		try {
			connection.publish(topic, String.valueOf(value).getBytes(),
					AT_LEAST_ONCE, false);
		} catch (Exception e) {
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
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long sendCustomMessage(String... messages) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		try {
			deregisterAllEventListeners();
			if (this.connection.isConnected()) {
				this.connection.disconnect();
			}
			super.close();
		} catch (Exception e) {
			throw propagate(e);
		}
	}

}
