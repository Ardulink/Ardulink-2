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
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.core.mqtt.MqttLinkConfig.Connection.SSL;
import static org.ardulink.core.mqtt.MqttLinkConfig.Connection.TCP;
import static org.ardulink.core.mqtt.MqttLinkConfig.Connection.TLS;
import static org.ardulink.util.Lists.rangeCheckedGet;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Strings.nullOrEmpty;
import static org.ardulink.util.Throwables.propagate;
import static org.ardulink.util.anno.LapsedWith.JDK9;
import static org.fusesource.mqtt.client.QoS.AT_LEAST_ONCE;
import static org.fusesource.mqtt.client.QoS.AT_MOST_ONCE;
import static org.fusesource.mqtt.client.QoS.EXACTLY_ONCE;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Pin.Type;
import org.ardulink.core.Tone;
import org.ardulink.core.mqtt.MqttLinkConfig.Connection;
import org.ardulink.core.proto.api.MessageIdHolders;
import org.ardulink.util.MapBuilder;
import org.ardulink.util.URIs;
import org.ardulink.util.anno.LapsedWith;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
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

	@LapsedWith(value = JDK9, module = "List#of")
	private static final Map<Connection, String> prefixes = unmodifiableMap(
			new EnumMap<>(MapBuilder.<Connection, String>newMapBuilder() //
					.put(TCP, "tcp") //
					.put(SSL, "ssl") //
					.put(TLS, "tls") //
					.build()));

	@LapsedWith(value = JDK9, module = "List#of")
	private static final Map<Type, String> typeMap = unmodifiableMap(
			new EnumMap<>(MapBuilder.<Type, String>newMapBuilder() //
					.put(Type.ANALOG, ANALOG) //
					.put(Type.DIGITAL, DIGITAL) //
					.build()));

	// We do not want to depend on the ordinal of a third-party enum class, so
	// (re-)define it here
	private static final List<QoS> qosLevels = unmodifiableList(asList(AT_MOST_ONCE, AT_LEAST_ONCE, EXACTLY_ONCE));

	private final QoS qos;
	private final String topic;
	private final Pattern mqttReceivePattern;
	private final MQTT mqttClient;
	private final BlockingConnection connection;
	private final boolean hasAppendix;

	public MqttLink(MqttLinkConfig config) throws IOException {
		checkArgument(config.getHost() != null, "host must not be null");
		checkArgument(config.getClientId() != null, "clientId must not be null");
		checkArgument(config.getTopic() != null, "topic must not be null");
		this.qos = rangeCheckedGet(qosLevels, config.qos, "qos level");
		this.hasAppendix = config.separateTopics;
		this.topic = config.getTopic();
		this.mqttReceivePattern = compile(MqttLink.this.topic + "([aAdD])(\\d+)" + quote(appendixSub()));
		this.mqttClient = newClient(config);
		this.mqttClient.setConnectAttemptsMax(1);
		this.connection = new BlockingConnection(new FutureConnection(newCallbackConnection()));
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
						messageReceived(connection.receive());
						connection.receive().ack();
					} catch (Exception e) {
						log.error("Error while waiting for new message", e);
					}
				}
			}

			private void messageReceived(Message message) {
				Matcher matcher = mqttReceivePattern.matcher(message.getTopic());
				if (matcher.matches() && matcher.groupCount() == 2) {
					String pinType = matcher.group(1);
					int pinNumber = parseInt(matcher.group(2));
					String payload = payload(message);
					if (DIGITAL.equalsIgnoreCase(pinType)) {
						fireStateChanged(digitalPinValueChanged(digitalPin(pinNumber), parseBoolean(payload)));
					} else if (ANALOG.equalsIgnoreCase(pinType)) {
						fireStateChanged(analogPinValueChanged(analogPin(pinNumber), parseInt(payload)));
					}
				}
			}

			private String payload(Message message) {
				return new String(message.getPayload(), UTF_8);
			}

		};
	}

	private CallbackConnection newCallbackConnection() {
		return new CallbackConnection(new MQTT(this.mqttClient)) {

			@Override
			public CallbackConnection listener(Listener listener) {
				return super.listener(multiplex(listener, connectionListener()));
			}

			private Listener multiplex(Listener... listeners) {
				return new Listener() {

					@Override
					public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
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
		client.setHost(URIs.newURI(connectionPrefix(config) + "://" + config.getHost() + ":" + config.port));
		String user = config.user;
		if (!nullOrEmpty(user)) {
			client.setUserName(user);
		}
		String password = config.password;
		if (!nullOrEmpty(password)) {
			client.setPassword(password);
		}
		return client;

	}

	private static String connectionPrefix(MqttLinkConfig config) {
		Connection connection = config.getConnection();
		return checkNotNull(prefixes.get(connection), "Could not resolve %s to prefix", connection);
	}

	private void subscribe() throws Exception {
		connection.subscribe(new Topic[] { new Topic(topic + "#", qos) });
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
		return topic + "system/listening/" + getType(pin) + pin.pinNum() + appendixPub();
	}

	private String getType(Pin pin) {
		return checkNotNull(typeMap.get(pin.getType()), "Cannot handle pin %s", pin);
	}

	@Override
	public long switchAnalogPin(AnalogPin analogPin, int value) throws IOException {
		switchPin(ANALOG, analogPin, value);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long switchDigitalPin(DigitalPin digitalPin, boolean value) throws IOException {
		switchPin(DIGITAL, digitalPin, value);
		return MessageIdHolders.NO_ID.getId();
	}

	private void switchPin(String type, Pin pin, Object value) throws IOException {
		publish(topic + type + pin.pinNum() + appendixPub(), value);
	}

	private void publish(String topic, Object value) throws IOException {
		try {
			connection.publish(topic, String.valueOf(value).getBytes(), qos, false);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public long sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex)
			throws IOException {
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
