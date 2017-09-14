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
import static java.lang.Integer.parseInt;
import static java.util.Collections.unmodifiableMap;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Throwables.propagate;
import static org.fusesource.mqtt.client.QoS.AT_LEAST_ONCE;

import java.io.IOException;
import java.util.EnumMap;
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
import org.ardulink.core.mqtt.MqttLinkConfig.Connection;
import org.ardulink.core.proto.api.MessageIdHolders;
import org.ardulink.util.MapBuilder;
import org.ardulink.util.Strings;
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
	private final BlockingConnection connection;
	private final boolean hasAppendix;

	private static final Map<Type, String> typeMap = unmodifiableMap(new EnumMap<Type, String>(
			MapBuilder.<Type, String> newMapBuilder().put(Type.ANALOG, ANALOG)
					.put(Type.DIGITAL, DIGITAL).build()));

	public MqttLink(MqttLinkConfig config) throws IOException {
		checkArgument(config.getHost() != null, "host must not be null");
		checkArgument(config.getClientId() != null, "clientId must not be null");
		checkArgument(config.getTopic() != null, "topic must not be null");
		this.hasAppendix = config.isSeparateTopics();
		this.topic = config.getTopic();
		this.mqttReceivePattern = Pattern.compile(MqttLink.this.topic
				+ "([aAdD])(\\d+)" + Pattern.quote(appendixSub()));
		this.mqttClient = newClient(config);
		this.mqttClient.setConnectAttemptsMax(1);
		this.connection = new BlockingConnection(new FutureConnection(
				newCallbackConnection()));
		newReceivedThread().start();
		try {
			connection.connect();
			subscribe();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private String appendixSub() {
		return hasAppendix ? "/value/get" : "";
	}

	private String appendixPub() {
		return hasAppendix ? "/value/set" : "";
	}

	private Thread newReceivedThread() {
		return new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Message message = connection.receive();
						Matcher matcher = mqttReceivePattern.matcher(message
								.getTopic());
						if (matcher.matches() && matcher.groupCount() == 2) {
							Pin pin = pin(matcher.group(1),
									parseInt(matcher.group(2)));
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

		};
	}

	private CallbackConnection newCallbackConnection() {
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
		client.setHost(URIs.newURI(connectionPrefix(config) + "://"
				+ config.getHost() + ":" + config.getPort()));
		String user = config.getUser();
		if (!Strings.nullOrEmpty(user)) {
			client.setUserName(user);
		}
		String password = config.getPassword();
		if (!Strings.nullOrEmpty(password)) {
			client.setPassword(password);
		}
		return client;

	}

	private static String connectionPrefix(MqttLinkConfig config) {
		Connection connection = config.getConnection();
		switch (connection) {
		case TCP:
			return "tcp";
		case SSL:
			return "ssl";
		case TLS:
			return "tls";
		}
		throw new IllegalStateException("Could not resolve " + connection);
	}

	private void subscribe() throws Exception {
		connection
				.subscribe(new Topic[] { new Topic(topic + "#", AT_LEAST_ONCE) });
	}

	@Override
	public long startListening(Pin pin) throws IOException {
		publish(controlTopic(pin), TRUE);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long stopListening(Pin pin) throws IOException {
		publish(controlTopic(pin), FALSE);
		return MessageIdHolders.NO_ID.getId();
	}

	private String controlTopic(Pin pin) {
		return topic + "system/listening/" + getType(pin) + pin.pinNum()
				+ appendixPub();
	}

	private String getType(Pin pin) {
		return checkNotNull(typeMap.get(pin.getType()), "Cannot handle pin %s",
				pin);
	}

	@Override
	public long switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		switchPin(ANALOG, analogPin, value);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		switchPin(DIGITAL, digitalPin, value);
		return MessageIdHolders.NO_ID.getId();
	}

	private void switchPin(String type, Pin pin, Object value)
			throws IOException {
		publish(topic + type + pin.pinNum() + appendixPub(), value);
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
	public long sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long sendTone(Tone tone) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long sendNoTone(AnalogPin analogPin) throws IOException {
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
