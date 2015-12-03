package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.github.pfichtner.core.mqtt.duplicated.EventMatchers.eventFor;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.events.DigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.EventListenerAdapter;
import com.github.pfichtner.ardulink.core.events.FilteredEventListenerAdapter;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.core.mqtt.duplicated.AnotherMqttClient;
import com.github.pfichtner.core.mqtt.duplicated.EventMatchers.PinValueChangedEventMatcher;
import com.github.pfichtner.core.mqtt.duplicated.Message;

public class MqttIntegrationTest {

	public static class EventCollector extends EventListenerAdapter {

		private final List<DigitalPinValueChangedEvent> events = new ArrayList<DigitalPinValueChangedEvent>();

		@Override
		public void stateChanged(DigitalPinValueChangedEvent event) {
			events.add(event);
		}

		public void assertReceived(PinValueChangedEventMatcher withValue) {
			assertThat(this.events, is(withValue));
		}

	}

	private static final String TOPIC = "myTopic/";

	private Server broker;
	private AnotherMqttClient amc;
	private Link link;

	@Before
	public void setup() throws Exception {
		broker = startBroker();
		amc = new AnotherMqttClient(TOPIC).connect();
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
		amc.disconnect();
		broker.stopServer();
	}

	@Test
	public void canSwitchDigitalPin() throws IOException {
		this.link.switchDigitalPin(digitalPin(30), true);
		assertThat(amc.getMessages(),
				is(Arrays.asList(new Message(TOPIC + "D30/set/value", "true"))));
	}

	@Test
	public void canSwitchAnalogPin() throws IOException {
		this.link.switchAnalogPin(analogPin(12), 34);
		assertThat(amc.getMessages(),
				is(Arrays.asList(new Message(TOPIC + "A12/set/value", "34"))));
	}

	@Test
	public void sendsControlMessageWhenAddingAnalogListener()
			throws IOException {
		this.link.addListener(new FilteredEventListenerAdapter(analogPin(1),
				delegate()));
		assertThat(
				amc.getMessages(),
				is(Arrays.asList(new Message(TOPIC
						+ "system/listening/A1/set/value", "true"))));
	}

	@Test
	public void sendsControlMessageWhenAddingDigitalListener()
			throws IOException {
		this.link.addListener(new FilteredEventListenerAdapter(digitalPin(2),
				delegate()));
		assertThat(
				amc.getMessages(),
				is(Arrays.asList(new Message(TOPIC
						+ "system/listening/D2/set/value", "true"))));
	}

	@Test
	public void sendsControlMessageWhenRemovingAnalogListener()
			throws IOException {
		EventListenerAdapter listener = new FilteredEventListenerAdapter(
				analogPin(1), delegate());
		this.link.addListener(listener);
		this.link.addListener(listener);
		Message m = new Message(TOPIC + "system/listening/A1/set/value", "true");
		// at the moment this is sent twice (see ListenerSupport)
		assertThat(amc.pollMessages(), is(Arrays.asList(m, m)));
		this.link.removeListener(listener);
		assertThat(amc.getMessages(), is(Collections.<Message> emptyList()));
		this.link.removeListener(listener);
		Message m2 = new Message(TOPIC + "system/listening/A1/set/value",
				"false");
		assertThat(amc.getMessages(), is(Arrays.asList(m2)));
	}

	@Test
	public void sendsControlMessageWhenRemovingDigitalListener()
			throws IOException {
		EventListenerAdapter listener = new FilteredEventListenerAdapter(
				digitalPin(1), delegate());
		this.link.addListener(listener);
		this.link.addListener(listener);
		Message m = new Message(TOPIC + "system/listening/D1/set/value", "true");
		// at the moment this is sent twice (see ListenerSupport)
		assertThat(amc.pollMessages(), is(Arrays.asList(m, m)));
		this.link.removeListener(listener);
		assertThat(amc.getMessages(), is(Collections.<Message> emptyList()));
		this.link.removeListener(listener);
		Message m2 = new Message(TOPIC + "system/listening/D1/set/value",
				"false");
		assertThat(amc.getMessages(), is(Arrays.asList(m2)));
	}

	// ---------------------------------------------------------------------------

	@Test
	public void canSwitchDigitalPinViaBroker() throws Exception {
		int pin = 1;
		boolean value = true;
		EventCollector eventCollector = new EventCollector();
		this.link.addListener(eventCollector);
		amc.sendMessage(new Message(TOPIC + "D" + pin + "/set/value", String
				.valueOf(value)));
		eventCollector.assertReceived(eventFor(digitalPin(pin))
				.withValue(value));
	}

	@Test
	public void canSwitchAnalogPinViaBroker() throws Exception {
		int pin = 2;
		int value = 123;
		EventCollector eventCollector = new EventCollector();
		this.link.addListener(eventCollector);
		amc.sendMessage(new Message(TOPIC + "A" + pin + "/set/value", String
				.valueOf(value)));
		eventCollector
				.assertReceived(eventFor(analogPin(pin)).withValue(value));
	}

	private EventListenerAdapter delegate() {
		return null;
	}

	private static Server startBroker() throws IOException,
			InterruptedException {
		Server broker = new Server();
		broker.startServer();
		return broker;
	}

}
