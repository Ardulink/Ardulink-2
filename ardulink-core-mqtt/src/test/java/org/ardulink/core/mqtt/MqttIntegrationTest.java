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

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.mqtt.duplicated.EventMatchers.eventFor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.rules.RuleChain.outerRule;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.Timeout;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.core.events.FilteredEventListenerAdapter;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.mqtt.duplicated.AnotherMqttClient;
import org.ardulink.core.mqtt.duplicated.Message;
import org.ardulink.util.URIs;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttIntegrationTest {

	private static final String TOPIC = "myTopic" + System.currentTimeMillis();

	private AnotherMqttClient mqttClient = AnotherMqttClient.newClient(TOPIC);

	@Rule
	public RuleChain chain = outerRule(Broker.newBroker()).around(mqttClient);

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	private Link link;

	@Before
	public void setup() throws Exception {
		this.link = LinkManager
				.getInstance()
				.getConfigurer(
						URIs.newURI("ardulink://mqtt?host=localhost&port=1883&topic="
								+ TOPIC)).newLink();
	}

	@After
	public void tearDown() throws InterruptedException, IOException {
		link.close();
	}

	@Test
	public void canSwitchDigitalPin() throws IOException {
		this.link.switchDigitalPin(digitalPin(30), true);
		assertThat(
				mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC + "/D30/value/set", "true"))));
	}

	@Test
	public void canSwitchAnalogPin() throws IOException {
		this.link.switchAnalogPin(analogPin(12), 34);
		assertThat(mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC + "/A12/value/set", "34"))));
	}

	@Test
	public void sendsControlMessageWhenAddingAnalogListener()
			throws IOException {
		this.link.addListener(new FilteredEventListenerAdapter(analogPin(1),
				delegate()));
		assertThat(
				mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC
						+ "/system/listening/A1/value/set", "true"))));
	}

	@Test
	public void sendsControlMessageWhenAddingDigitalListener()
			throws IOException {
		this.link.addListener(new FilteredEventListenerAdapter(digitalPin(2),
				delegate()));
		assertThat(
				mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC
						+ "/system/listening/D2/value/set", "true"))));
	}

	@Test
	public void sendsControlMessageWhenRemovingAnalogListener()
			throws IOException {
		EventListenerAdapter listener = new FilteredEventListenerAdapter(
				analogPin(1), delegate());
		this.link.addListener(listener);
		this.link.addListener(listener);
		Message m = new Message(TOPIC + "/system/listening/A1/value/set",
				"true");
		// at the moment this is sent twice (see ListenerSupport)
		assertThat(mqttClient.pollMessages(), is(Arrays.asList(m, m)));
		this.link.removeListener(listener);
		assertThat(mqttClient.getMessages(),
				is(Collections.<Message> emptyList()));
		this.link.removeListener(listener);
		Message m2 = new Message(TOPIC + "/system/listening/A1/value/set",
				"false");
		assertThat(mqttClient.getMessages(), is(Arrays.asList(m2)));
	}

	@Test
	public void sendsControlMessageWhenRemovingDigitalListener()
			throws IOException {
		EventListenerAdapter listener = new FilteredEventListenerAdapter(
				digitalPin(1), delegate());
		this.link.addListener(listener);
		this.link.addListener(listener);
		Message m = new Message(TOPIC + "/system/listening/D1/value/set",
				"true");
		// at the moment this is sent twice (see ListenerSupport)
		assertThat(mqttClient.pollMessages(), is(Arrays.asList(m, m)));
		this.link.removeListener(listener);
		assertThat(mqttClient.getMessages(),
				is(Collections.<Message> emptyList()));
		this.link.removeListener(listener);
		Message m2 = new Message(TOPIC + "/system/listening/D1/value/set",
				"false");
		assertThat(mqttClient.getMessages(), is(Arrays.asList(m2)));
	}

	// ---------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Test
	public void canSwitchDigitalPinViaBroker() throws Exception {
		Pin pin = digitalPin(1);
		boolean value = true;
		EventCollector eventCollector = new EventCollector();
		this.link.addListener(eventCollector);
		mqttClient.switchPin(pin, value);
		assertThat(eventCollector.events(DIGITAL), hasItems(eventFor(pin)
				.withValue(value)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void canSwitchAnalogPinViaBroker() throws Exception {
		Pin pin = analogPin(2);
		int value = 123;
		EventCollector eventCollector = new EventCollector();
		this.link.addListener(eventCollector);
		mqttClient.switchPin(pin, value);
		assertThat(eventCollector.events(ANALOG), hasItems(eventFor(pin)
				.withValue(value)));
	}

	private EventListenerAdapter delegate() {
		return null;
	}

}
