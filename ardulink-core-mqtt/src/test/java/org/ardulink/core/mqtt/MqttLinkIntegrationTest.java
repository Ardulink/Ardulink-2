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

import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.core.mqtt.EventCollector.eventCollector;
import static org.ardulink.testsupport.mock.TestSupport.extractDelegated;
import static org.ardulink.util.ServerSockets.freePort;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.ConnectionListener;
import org.ardulink.core.Link;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.mqtt.duplicated.AnotherMqttClient;
import org.ardulink.core.mqtt.duplicated.Message;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Timeout(value = 2, unit = MINUTES)
class MqttLinkIntegrationTest {

	private static class TestConfig {

		private String name;
		private boolean separateTopics;
		private String messageFormat;

		public TestConfig(String name, boolean separateTopics, String messageFormat) {
			this.name = name;
			this.separateTopics = separateTopics;
			this.messageFormat = messageFormat;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	public static class TrackStateConnectionListener implements ConnectionListener {

		private AtomicBoolean connected = new AtomicBoolean(true);

		public AtomicBoolean isConnected() {
			return connected;
		}

		@Override
		public void connectionLost() {
			this.connected.set(false);
		}

		@Override
		public void reconnected() {
			this.connected.set(true);
		}

	}

	static final String TOPIC = "myTopic" + System.currentTimeMillis();

	@RegisterExtension
	Broker broker = Broker.newBroker().port(freePort());

	@RegisterExtension
	AnotherMqttClient mqttClient = AnotherMqttClient.newClient(TOPIC, broker.port());

	String messageFormat;

	boolean separatedTopics;

	private static List<TestConfig> data() {
		return asList( //
				new TestConfig("sameTopic", false, TOPIC + "/%s"), //
				new TestConfig("separateTopics", true, TOPIC + "/%s/value/set") //
		);
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void defaultHostIsLocalhostAndLinkHasCreatedWithoutConfiguring(TestConfig testConfig) throws IOException {
		try (Link link = makeLink(testConfig)) {
			assertThat(link).isNotNull();
		}
	}

	EventCollector eventCollector = eventCollector();

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canSendToBrokerAfterReconnect(TestConfig testConfig) throws Exception {
		try (Link link = makeLink(testConfig)) {
			breedReconnectedState(link);

			link.switchAnalogPin(analogPin(8), 9);
			mqttClient.awaitMessages(m -> assertThat(m).singleElement().isEqualTo(new Message(topic("A8"), "9")));
		}
	}

	String topic(String pin) {
		return String.format(messageFormat, pin);
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canReceiveFromBrokerAfterReconnect(TestConfig testConfig) throws Exception {
		try (Link link = makeLink(testConfig)) {
			breedReconnectedState(link);

			DigitalPin pin = digitalPin(2);
			boolean value = true;
			mqttClient.switchPin(pin, value);
			eventCollector.awaitEvents(DIGITAL, l -> l.contains(digitalPinValueChanged(pin, value)));
		}
	}

	private void breedReconnectedState(Link link) throws IOException {
		TrackStateConnectionListener connectionListener = new TrackStateConnectionListener();
		((AbstractListenerLink) extractDelegated(link)).addConnectionListener(connectionListener);
		try {
			assertThat(connectionListener.isConnected()).isTrue();
			restartBrokerAndWaitForReconnect(connectionListener);
		} finally {
			((AbstractListenerLink) extractDelegated(link)).removeConnectionListener(connectionListener);
		}
	}

	private Link makeLink(TestConfig config) throws IOException {
		this.separatedTopics = config.separateTopics;
		this.messageFormat = config.messageFormat;
		this.mqttClient.appendValueSet(config.separateTopics);
		Link link = Links.getLink(
				"ardulink://mqtt?port=" + broker.port() + "&topic=" + TOPIC + "&separatedTopics=" + separatedTopics);
		link.addListener(eventCollector);
		return link;
	}

	private void restartBrokerAndWaitForReconnect(TrackStateConnectionListener connectionListener) throws IOException {
		this.broker.stop();
		awaitConnectionIs(connectionListener, false);
		await("mqttClient lost connection").until(() -> !this.mqttClient.isConnected());

		this.broker.start();
		awaitConnectionIs(connectionListener, true);
		await("mqttClient reconnected").until(() -> this.mqttClient.isConnected());
	}

	private void awaitConnectionIs(TrackStateConnectionListener connectionListener, boolean connected) {
		await("await connectionListener == " + connected).until(connectionListener.isConnected()::get,
				t -> t == connected);
	}

	private static ConditionFactory await(String alias) {
		return Awaitility.await(alias).timeout(2, MINUTES).pollInterval(ofMillis(100));
	}

}
