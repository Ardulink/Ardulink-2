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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.mqtt.duplicated.EventMatchers.eventFor;
import static org.ardulink.util.ServerSockets.freePort;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.RuleChain.outerRule;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ardulink.core.ConnectionListener;
import org.ardulink.core.events.PinValueChangedEvent;
import org.ardulink.core.mqtt.duplicated.AnotherMqttClient;
import org.ardulink.core.mqtt.duplicated.EventMatchers.PinValueChangedEventMatcher;
import org.ardulink.core.mqtt.duplicated.Message;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@RunWith(Parameterized.class)
public class MqttLinkIntegrationTest {

	public static class TrackStateConnectionListener implements
			ConnectionListener {

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

	private static final String TOPIC = "myTopic" + System.currentTimeMillis();

	private final Broker broker = Broker.newBroker().port(freePort());

	private final AnotherMqttClient mqttClient = AnotherMqttClient.newClient(
			TOPIC, broker.getPort());

	private final String messageFormat;

	private final boolean separateTopics;

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public RuleChain chain = outerRule(broker).around(mqttClient);

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { sameTopic(), separateTopics() });
	}

	private static Object[] sameTopic() {
		return new Object[] { "sameTopic", false, TOPIC + "/%s" };
	}

	private static Object[] separateTopics() {
		return new Object[] { "separateTopics", true, TOPIC + "/%s/value/set" };
	}

	public MqttLinkIntegrationTest(String name, boolean separateTopics,
			String messageFormat) {
		this.separateTopics = separateTopics;
		this.mqttClient.appendValueSet(separateTopics);
		this.messageFormat = messageFormat;
	}

	@Test
	public void defaultHostIsLocalhostAndLinkHasCreatedWithoutConfiguring()
			throws UnknownHostException, IOException {
		MqttLinkFactory factory = new MqttLinkFactory();
		MqttLinkConfig config = makeConfig(factory);
		String host = config.getHost();
		assertThat(host, is("localhost"));
		MqttLink link = factory.newLink(config);
		assertThat(link, notNullValue());
		link.close();
	}

	@Test
	public void canSendToBrokerAfterReconnect() throws Exception {
		EventCollector eventCollector = new EventCollector();
		MqttLink link = makeLink(eventCollector);
		breedReconnectedState(link);

		link.switchAnalogPin(analogPin(8), 9);
		assertThat(mqttClient.getMessages(),
				is(Arrays.asList(new Message(topic("A8"), "9"))));
		link.close();
	}

	private String topic(String pin) {
		return String.format(messageFormat, pin);
	}

	@Test
	public void canReceiveFromBrokerAfterReconnect() throws Exception {
		EventCollector eventCollector = new EventCollector();
		MqttLink link = makeLink(eventCollector);
		breedReconnectedState(link);

		mqttClient.switchPin(digitalPin(2), true);
		assertThat(eventCollector.events(DIGITAL),
				hasItems(eventFor(digitalPin(2)).withValue(true)));
		link.close();
	}

	private void breedReconnectedState(MqttLink link) throws IOException,
			InterruptedException {
		TrackStateConnectionListener connectionListener = new TrackStateConnectionListener();
		link.addConnectionListener(connectionListener);
		assertThat(connectionListener.isConnected(), is(true));

		restartBroker(connectionListener);
		waitForLinkReconnect(connectionListener);
	}

	private MqttLink makeLink(EventCollector eventCollector)
			throws UnknownHostException, IOException {
		MqttLinkFactory factory = new MqttLinkFactory();
		MqttLink link = factory.newLink(makeConfig(factory));
		link.addListener(eventCollector);
		return link;
	}

	private MqttLinkConfig makeConfig(MqttLinkFactory factory) {
		MqttLinkConfig config = factory.newLinkConfig();
		config.setTopic(TOPIC);
		config.setPort(broker.getPort());
		config.setSeparateTopics(separateTopics);
		return config;
	}

	public void waitForLinkReconnect(
			TrackStateConnectionListener connectionListener)
			throws InterruptedException {
		while (!connectionListener.isConnected()) {
			MILLISECONDS.sleep(100);
		}
	}

	public void restartBroker(TrackStateConnectionListener connectionListener)
			throws InterruptedException, IOException {
		this.broker.stop();
		while (connectionListener.isConnected()) {
			MILLISECONDS.sleep(100);
		}
		this.broker.start();
	}

	private static Matcher<? super List<PinValueChangedEvent>> hasItems(
			PinValueChangedEventMatcher... matchers) {
		return IsCollectionContaining.hasItems(matchers);
	}

}
