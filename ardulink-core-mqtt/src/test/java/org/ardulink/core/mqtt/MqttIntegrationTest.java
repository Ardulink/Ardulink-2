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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.mqtt.duplicated.EventMatchers.eventFor;
import static org.ardulink.util.ServerSockets.freePort;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.rules.RuleChain.outerRule;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.core.events.FilteredEventListenerAdapter;
import org.ardulink.core.mqtt.duplicated.AnotherMqttClient;
import org.ardulink.core.mqtt.duplicated.Message;
import org.junit.After;
import org.junit.Before;
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
public class MqttIntegrationTest {

	private static final String TOPIC = "myTopic" + System.currentTimeMillis();

	private final Broker broker = Broker.newBroker().port(freePort());
	
	private AnotherMqttClient mqttClient = AnotherMqttClient.newClient(TOPIC, broker.getPort());

	@Rule
	public RuleChain chain = outerRule(broker).around(mqttClient);

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	private Link link;

	private final String messageFormat;

	private String clientUri;

	@Before
	public void setup() {
		// must not be initialized at declaration point since then the broker is
		// not started and so the client can't connect!
		link = Links.getLink(clientUri);
	}

	@After
	public void tearDown() throws InterruptedException, IOException {
		link.close();
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return asList(new Object[][] { sameTopic(), separateTopics() });
	}

	private static Object[] sameTopic() {
		return new Object[] { "sameTopic", false, TOPIC + "/%s" };
	}

	private static Object[] separateTopics() {
		return new Object[] { "separateTopics", true, TOPIC + "/%s/value/set" };
	}

	public MqttIntegrationTest(String name, boolean separateTopics,
			String messageFormat) {
		this.mqttClient.appendValueSet(separateTopics);
		this.messageFormat = messageFormat;
		this.clientUri = "ardulink://mqtt?host=localhost&port="
				+ broker.getPort() + "&topic=" + TOPIC + "&separatedTopics="
				+ separateTopics;
	}

	@Test
	public void canSwitchDigitalPin() throws IOException {
		link.switchDigitalPin(digitalPin(30), true);
		mqttClient.awaitMessages(is(asList(new Message(topic("D30"), "true"))));
	}

	@Test
	public void canSwitchAnalogPin() throws IOException {
		link.switchAnalogPin(analogPin(12), 34);
		mqttClient.awaitMessages(is(asList(new Message(topic("A12"), "34"))));
	}

	@Test
	public void sendsControlMessageWhenAddingAnalogListener()
			throws IOException {
		link.addListener(new FilteredEventListenerAdapter(analogPin(1),
				delegate()));
		mqttClient.awaitMessages(is(asList(new Message(
				topic("system/listening/A1"), "true"))));
	}

	@Test
	public void sendsControlMessageWhenAddingDigitalListener()
			throws IOException {
		link.addListener(new FilteredEventListenerAdapter(digitalPin(2),
				delegate()));
		mqttClient.awaitMessages(is(asList(new Message(
				topic("system/listening/D2"), "true"))));
	}

	@Test
	public void sendsControlMessageWhenRemovingAnalogListener()
			throws IOException {
		EventListenerAdapter listener = new FilteredEventListenerAdapter(
				analogPin(1), delegate());
		link.addListener(listener);
		link.addListener(listener);
		Message m1 = new Message(topic("system/listening/A1"), "true");
		// at the moment this is sent twice (see AbstractListenerLink)
		mqttClient.awaitMessages(is(asList(m1, m1)));
		mqttClient.clear();
		link.removeListener(listener);
		mqttClient.awaitMessages(is(Collections.<Message> emptyList()));
		link.removeListener(listener);
		Message m2 = new Message(topic("system/listening/A1"), "false");
		mqttClient.awaitMessages(is(is(asList(m2))));
	}

	@Test
	public void sendsControlMessageWhenRemovingDigitalListener()
			throws IOException {
		EventListenerAdapter listener = new FilteredEventListenerAdapter(
				digitalPin(1), delegate());
		link.addListener(listener);
		link.addListener(listener);
		Message m1 = new Message(topic("system/listening/D1"), "true");
		// at the moment this is sent twice (see AbstractListenerLink)
		mqttClient.awaitMessages(is(asList(m1, m1)));
		mqttClient.clear();
		link.removeListener(listener);
		mqttClient.awaitMessages(is(Collections.<Message> emptyList()));
		link.removeListener(listener);
		Message m2 = new Message(topic("system/listening/D1"), "false");
		mqttClient.awaitMessages(is(asList(m2)));
	}

	private String topic(String pin) {
		return String.format(messageFormat, pin);
	}

	// ---------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Test
	public void canSwitchDigitalPinViaBroker() throws Exception {
		Pin pin = digitalPin(1);
		boolean value = true;
		EventCollector eventCollector = new EventCollector();
		link.addListener(eventCollector);
		mqttClient.switchPin(pin, value);
		eventCollector.awaitEvents(DIGITAL, hasItems(eventFor(pin)
				.withValue(value)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void canSwitchAnalogPinViaBroker() throws Exception {
		Pin pin = analogPin(2);
		int value = 123;
		EventCollector eventCollector = new EventCollector();
		link.addListener(eventCollector);
		mqttClient.switchPin(pin, value);
		eventCollector.awaitEvents(ANALOG, hasItems(eventFor(pin)
				.withValue(value)));
	}

	private EventListenerAdapter delegate() {
		return null;
	}

}
