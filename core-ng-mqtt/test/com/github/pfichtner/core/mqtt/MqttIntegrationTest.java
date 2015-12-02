package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.events.EventListenerAdapter;
import com.github.pfichtner.ardulink.core.events.FilteredEventListenerAdapter;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;

public class MqttIntegrationTest {

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
