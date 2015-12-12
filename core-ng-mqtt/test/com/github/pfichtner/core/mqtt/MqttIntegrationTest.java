package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.github.pfichtner.ardulink.core.Pin.Type.ANALOG;
import static com.github.pfichtner.ardulink.core.Pin.Type.DIGITAL;
import static com.github.pfichtner.core.mqtt.duplicated.EventMatchers.eventFor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.rules.RuleChain.outerRule;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.events.EventListenerAdapter;
import com.github.pfichtner.ardulink.core.events.FilteredEventListenerAdapter;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.core.mqtt.duplicated.AnotherMqttClient;
import com.github.pfichtner.core.mqtt.duplicated.Message;

public class MqttIntegrationTest {

	private static final String TOPIC = "myTopic" + System.currentTimeMillis();

	private AnotherMqttClient mqttClient = new AnotherMqttClient(TOPIC);

	@Rule
	public RuleChain chain = outerRule(new Broker()).around(mqttClient);

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	private Link link;

	@Before
	public void setup() throws Exception {
		this.link = LinkManager
				.getInstance()
				.getConfigurer(
						new URI(
								"ardulink://mqtt?host=localhost&port=1883&topic="
										+ TOPIC)).newLink();
	}

	@After
	public void tearDown() throws InterruptedException, MqttException,
			IOException {
		link.close();
	}

	@Test
	public void canSwitchDigitalPin() throws IOException {
		this.link.switchDigitalPin(digitalPin(30), true);
		assertThat(
				mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC + "/D30/set/value", "true"))));
	}

	@Test
	public void canSwitchAnalogPin() throws IOException {
		this.link.switchAnalogPin(analogPin(12), 34);
		assertThat(mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC + "/A12/set/value", "34"))));
	}

	@Test
	public void sendsControlMessageWhenAddingAnalogListener()
			throws IOException {
		this.link.addListener(new FilteredEventListenerAdapter(analogPin(1),
				delegate()));
		assertThat(
				mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC
						+ "/system/listening/A1/set/value", "true"))));
	}

	@Test
	public void sendsControlMessageWhenAddingDigitalListener()
			throws IOException {
		this.link.addListener(new FilteredEventListenerAdapter(digitalPin(2),
				delegate()));
		assertThat(
				mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC
						+ "/system/listening/D2/set/value", "true"))));
	}

	@Test
	public void sendsControlMessageWhenRemovingAnalogListener()
			throws IOException {
		EventListenerAdapter listener = new FilteredEventListenerAdapter(
				analogPin(1), delegate());
		this.link.addListener(listener);
		this.link.addListener(listener);
		Message m = new Message(TOPIC + "/system/listening/A1/set/value",
				"true");
		// at the moment this is sent twice (see ListenerSupport)
		assertThat(mqttClient.pollMessages(), is(Arrays.asList(m, m)));
		this.link.removeListener(listener);
		assertThat(mqttClient.getMessages(),
				is(Collections.<Message> emptyList()));
		this.link.removeListener(listener);
		Message m2 = new Message(TOPIC + "/system/listening/A1/set/value",
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
		Message m = new Message(TOPIC + "/system/listening/D1/set/value",
				"true");
		// at the moment this is sent twice (see ListenerSupport)
		assertThat(mqttClient.pollMessages(), is(Arrays.asList(m, m)));
		this.link.removeListener(listener);
		assertThat(mqttClient.getMessages(),
				is(Collections.<Message> emptyList()));
		this.link.removeListener(listener);
		Message m2 = new Message(TOPIC + "/system/listening/D1/set/value",
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
