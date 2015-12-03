package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.github.pfichtner.ardulink.core.Pin.Type.ANALOG;
import static com.github.pfichtner.ardulink.core.Pin.Type.DIGITAL;
import static com.github.pfichtner.core.mqtt.duplicated.EventMatchers.eventFor;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.RuleChain.outerRule;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.zu.ardulink.util.ListMultiMap;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.Type;
import com.github.pfichtner.ardulink.core.events.AnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.EventListener;
import com.github.pfichtner.ardulink.core.events.EventListenerAdapter;
import com.github.pfichtner.ardulink.core.events.FilteredEventListenerAdapter;
import com.github.pfichtner.ardulink.core.events.PinValueChangedEvent;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.core.mqtt.duplicated.AnotherMqttClient;
import com.github.pfichtner.core.mqtt.duplicated.Message;

public class MqttIntegrationTest {

	public static class EventCollector implements EventListener {

		private final ListMultiMap<Type, PinValueChangedEvent> events = new ListMultiMap<Type, PinValueChangedEvent>();

		@Override
		public void stateChanged(AnalogPinValueChangedEvent event) {
			events.put(ANALOG, event);
		}

		@Override
		public void stateChanged(DigitalPinValueChangedEvent event) {
			events.put(DIGITAL, event);
		}

		private List<PinValueChangedEvent> events(Type type) {
			try {
				TimeUnit.MILLISECONDS.sleep(25);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return events.asMap().get(type);
		}

	}

	private static final String TOPIC = "myTopic/";

	private AnotherMqttClient mqttClient = new AnotherMqttClient(TOPIC);

	@Rule
	public RuleChain chain = outerRule(new Broker()).around(mqttClient);

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
		assertThat(mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC + "D30/set/value", "true"))));
	}

	@Test
	public void canSwitchAnalogPin() throws IOException {
		this.link.switchAnalogPin(analogPin(12), 34);
		assertThat(mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC + "A12/set/value", "34"))));
	}

	@Test
	public void sendsControlMessageWhenAddingAnalogListener()
			throws IOException {
		this.link.addListener(new FilteredEventListenerAdapter(analogPin(1),
				delegate()));
		assertThat(
				mqttClient.getMessages(),
				is(Arrays.asList(new Message(TOPIC
						+ "system/listening/A1/set/value", "true"))));
	}

	@Test
	public void sendsControlMessageWhenAddingDigitalListener()
			throws IOException {
		this.link.addListener(new FilteredEventListenerAdapter(digitalPin(2),
				delegate()));
		assertThat(
				mqttClient.getMessages(),
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
		assertThat(mqttClient.pollMessages(), is(Arrays.asList(m, m)));
		this.link.removeListener(listener);
		assertThat(mqttClient.getMessages(),
				is(Collections.<Message> emptyList()));
		this.link.removeListener(listener);
		Message m2 = new Message(TOPIC + "system/listening/A1/set/value",
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
		Message m = new Message(TOPIC + "system/listening/D1/set/value", "true");
		// at the moment this is sent twice (see ListenerSupport)
		assertThat(mqttClient.pollMessages(), is(Arrays.asList(m, m)));
		this.link.removeListener(listener);
		assertThat(mqttClient.getMessages(),
				is(Collections.<Message> emptyList()));
		this.link.removeListener(listener);
		Message m2 = new Message(TOPIC + "system/listening/D1/set/value",
				"false");
		assertThat(mqttClient.getMessages(), is(Arrays.asList(m2)));
	}

	// ---------------------------------------------------------------------------

	@Test
	public void canSwitchDigitalPinViaBroker() throws Exception {
		Pin pin = digitalPin(1);
		boolean value = true;
		EventCollector eventCollector = new EventCollector();
		this.link.addListener(eventCollector);
		mqttClient.switchPin(pin, value);
		assertThat(eventCollector.events(DIGITAL),
				is(eventFor(pin).withValue(value)));
	}

	@Test
	public void canSwitchAnalogPinViaBroker() throws Exception {
		Pin pin = analogPin(2);
		int value = 123;
		EventCollector eventCollector = new EventCollector();
		this.link.addListener(eventCollector);
		mqttClient.switchPin(pin, value);
		assertThat(eventCollector.events(ANALOG),
				is(eventFor(pin).withValue(value)));
	}

	private EventListenerAdapter delegate() {
		return null;
	}

}
