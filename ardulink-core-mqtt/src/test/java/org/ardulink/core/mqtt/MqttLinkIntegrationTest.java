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

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.mqtt.duplicated.EventMatchers.eventFor;
import static org.ardulink.util.ServerSockets.freePort;
import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.ardulink.core.ConnectionListener;
import org.ardulink.core.events.PinValueChangedEvent;
import org.ardulink.core.mqtt.duplicated.AnotherMqttClient;
import org.ardulink.core.mqtt.duplicated.EventMatchers.PinValueChangedEventMatcher;
import org.ardulink.core.mqtt.duplicated.Message;
import org.ardulink.util.anno.LapsedWith;
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
@Timeout(30)
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

		private boolean connected = true;

		public boolean isConnected() {
			return connected;
		}

		@Override
		public void connectionLost() {
			this.connected = false;
		}

		@Override
		public void reconnected() {
			this.connected = true;
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
		this.mqttClient.appendValueSet(config.separateTopics);
		this.messageFormat = config.messageFormat;
		String clientUri = "ardulink://mqtt?host=localhost&port=" + broker.getPort() + "&topic=" + TOPIC
				+ "&separatedTopics=" + config.separateTopics;
	}
	
	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void defaultHostIsLocalhostAndLinkHasCreatedWithoutConfiguring(TestConfig testConfig) throws UnknownHostException, IOException {
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
		mqttClient.awaitMessages(is(asList(new Message(topic("A8"), "9"))));
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

	private void breedReconnectedState(MqttLink link) throws IOException, InterruptedException {
		TrackStateConnectionListener connectionListener = new TrackStateConnectionListener();
		link.addConnectionListener(connectionListener);
		assertThat(connectionListener.isConnected(), is(true));

		restartBroker(connectionListener);
		waitForLinkReconnect(connectionListener);
	}

	private MqttLink makeLink(EventCollector eventCollector) throws UnknownHostException, IOException {
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

	@LapsedWith(module = JDK8, value = "org.awaitability")
	public void waitForLinkReconnect(TrackStateConnectionListener connectionListener) throws InterruptedException {
		while (!connectionListener.isConnected()) {
			MILLISECONDS.sleep(100);
		}
	}

	@LapsedWith(module = JDK8, value = "org.awaitability")
	public void restartBroker(TrackStateConnectionListener connectionListener)
			throws InterruptedException, IOException {
		this.broker.stop();
		while (connectionListener.isConnected()) {
			MILLISECONDS.sleep(100);
		}
		this.broker.start();
	}

	private static Matcher<? super List<PinValueChangedEvent>> hasItems(PinValueChangedEventMatcher... matchers) {
		return IsCollectionContaining.hasItems(matchers);
	}

}
