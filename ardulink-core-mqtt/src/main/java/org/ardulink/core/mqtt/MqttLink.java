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
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Predicates.not;
import static org.ardulink.util.Throwables.propagate;
import static org.ardulink.util.anno.LapsedWith.JDK9;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
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
import org.ardulink.util.Throwables;
import org.ardulink.util.anno.LapsedWith;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttLink extends AbstractListenerLink {

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

	private final int qos;
	private final String topic;
	private final Pattern mqttReceivePattern;
	private final MqttClient mqttClient;
	private final boolean hasAppendix;

	public MqttLink(MqttLinkConfig config) throws MqttException {
		checkArgument(config.getHost() != null, "host must not be null");
		checkArgument(config.getClientId() != null, "clientId must not be null");
		checkArgument(config.getTopic() != null, "topic must not be null");
		this.qos = config.qos.level();
		this.hasAppendix = config.separateTopics;
		this.topic = config.getTopic();
		this.mqttReceivePattern = compile(MqttLink.this.topic + "([aAdD])(\\d+)" + quote(appendixSub()));
		this.mqttClient = newClient(config);
		MqttCallback callback = callback();
		this.mqttClient.setCallback(callback);
		this.mqttClient.connect(options(config));
		subscribe();
		fireReconnected();
	}

	private void subscribe() throws MqttException {
		this.mqttClient.subscribe(topic + "#", qos, (t, m) -> messageReceived(t, m));
	}

	private MqttCallback callback() {
		return new MqttCallbackExtended() {

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				// noop
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				// noop
			}

			@Override
			public void connectionLost(Throwable cause) {
				fireConnectionLost();
			}

			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				fireReconnected();
				try {
					subscribe();
				} catch (MqttException e) {
					throw Throwables.propagate(e);
				}
			}
		};
	}

	private static MqttConnectOptions options(MqttLinkConfig config) {
		MqttConnectOptions options = new MqttConnectOptions();
		options.setConnectionTimeout(10);
		options.setAutomaticReconnect(true);
		nonEmpty(config.user).ifPresent(options::setUserName);
		nonEmpty(config.password).map(String::toCharArray).ifPresent(options::setPassword);
		return options;
	}

	private static Optional<String> nonEmpty(String user) {
		return Optional.ofNullable(user).filter(not(String::isEmpty));
	}

	private String appendixSub() {
		return hasAppendix ? "/value/get" : "";
	}

	private String appendixPub() {
		return hasAppendix ? "/value/set" : "";
	}

	private void messageReceived(String topic, MqttMessage message) {
		Matcher matcher = mqttReceivePattern.matcher(topic);
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

	private String payload(MqttMessage message) {
		return new String(message.getPayload(), UTF_8);
	}

	private MqttClient newClient(MqttLinkConfig config) throws MqttException {
		return new MqttClient(serverUrl(config), config.getClientId(), new MemoryPersistence());
	}

	private String serverUrl(MqttLinkConfig config) {
		return connectionPrefix(config) + "://" + config.getHost() + ":" + config.port;
	}

	private static String connectionPrefix(MqttLinkConfig config) {
		Connection connection = config.getConnection();
		return checkNotNull(prefixes.get(connection), "Could not resolve %s to prefix", connection);
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
			this.mqttClient.publish(topic, String.valueOf(value).getBytes(), qos, false);
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
			if (this.mqttClient.isConnected()) {
				this.mqttClient.disconnect();
			}
			super.close();
		} catch (Exception e) {
			throw propagate(e);
		}
	}

}
