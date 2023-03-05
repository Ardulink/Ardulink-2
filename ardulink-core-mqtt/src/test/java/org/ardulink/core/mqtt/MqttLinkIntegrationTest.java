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
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.mqtt.duplicated.EventMatchers.eventFor;
import static org.ardulink.util.ServerSockets.freePort;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardulink.core.ConnectionListener;
import org.ardulink.core.events.PinValueChangedEvent;
import org.ardulink.core.mqtt.duplicated.AnotherMqttClient;
import org.ardulink.core.mqtt.duplicated.EventMatchers.PinValueChangedEventMatcher;
import org.ardulink.core.mqtt.duplicated.Message;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsCollectionContaining;
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
@Timeout(MqttLinkIntegrationTest.TIMEOUT)
class MqttLinkIntegrationTest {

	static final int TIMEOUT = 30;

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
	AnotherMqttClient mqttClient = AnotherMqttClient.newClient(TOPIC, broker.getPort());

	String messageFormat;

	boolean separateTopics;

	private static List<TestConfig> data() {
		return asList( //
				new TestConfig("sameTopic", false, TOPIC + "/%s"), //
				new TestConfig("separateTopics", true, TOPIC + "/%s/value/set") //
		);
	}

	void init(TestConfig config) {
		this.separateTopics = config.separateTopics;
		this.messageFormat = config.messageFormat;
		this.mqttClient.appendValueSet(config.separateTopics);
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void defaultHostIsLocalhostAndLinkHasCreatedWithoutConfiguring(TestConfig testConfig)
			throws IOException {
		init(testConfig);
		MqttLinkFactory factory = new MqttLinkFactory();
		MqttLinkConfig config = makeConfig(factory);
		String host = config.getHost();
		assertThat(host, is("localhost"));
		MqttLink link = factory.newLink(config);
		assertThat(link, notNullValue());
		link.close();
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canSendToBrokerAfterReconnect(TestConfig testConfig) throws Exception {
		init(testConfig);
		EventCollector eventCollector = new EventCollector();
		MqttLink link = makeLink(eventCollector);
		breedReconnectedState(link);

		link.switchAnalogPin(analogPin(8), 9);
		mqttClient.awaitMessages(is(Collections.singletonList(new Message(topic("A8"), "9"))));
		link.close();
	}

	private String topic(String pin) {
		return String.format(messageFormat, pin);
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canReceiveFromBrokerAfterReconnect(TestConfig testConfig) throws Exception {
		init(testConfig);
		EventCollector eventCollector = new EventCollector();
		MqttLink link = makeLink(eventCollector);
		breedReconnectedState(link);

		mqttClient.switchPin(digitalPin(2), true);
		eventCollector.awaitEvents(DIGITAL, hasItems(eventFor(digitalPin(2)).withValue(true)));
		link.close();
	}

	private void breedReconnectedState(MqttLink link) throws IOException {
		TrackStateConnectionListener connectionListener = new TrackStateConnectionListener();
		link.addConnectionListener(connectionListener);
		assertThat(connectionListener.isConnected().get(), is(true));

		restartBroker(connectionListener);
		waitForLinkReconnect(connectionListener);
	}

	private MqttLink makeLink(EventCollector eventCollector) throws IOException {
		MqttLinkFactory factory = new MqttLinkFactory();
		MqttLink link = factory.newLink(makeConfig(factory));
		link.addListener(eventCollector);
		return link;
	}

	private MqttLinkConfig makeConfig(MqttLinkFactory factory) {
		MqttLinkConfig config = factory.newLinkConfig();
		config.setTopic(TOPIC);
		config.port = broker.getPort();
		config.separateTopics = separateTopics;
		return config;
	}

	private void waitForLinkReconnect(TrackStateConnectionListener connectionListener) {
		awaitConnectionIs(connectionListener, is(true));
	}

	private void restartBroker(TrackStateConnectionListener connectionListener) throws IOException {
		this.broker.stop();
		awaitConnectionIs(connectionListener, is(false));
		this.broker.start();
	}

	private void awaitConnectionIs(TrackStateConnectionListener connectionListener, Matcher<Boolean> matcher) {
		await().timeout(ofSeconds(TIMEOUT * 2)).pollInterval(ofMillis(100))
				.untilAtomic(connectionListener.isConnected(), matcher);
	}

	private static Matcher<? super List<PinValueChangedEvent>> hasItems(PinValueChangedEventMatcher... matchers) {
		return IsCollectionContaining.hasItems(matchers);
	}

}
