package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.Pin;
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
		this.link.switchAnalogPin(Pin.analogPin(12), 34);
		assertThat(amc.getMessages(),
				is(Arrays.asList(new Message(TOPIC + "A12/set/value", "34"))));
	}

	private static Server startBroker() throws IOException,
			InterruptedException {
		Server broker = new Server();
		broker.startServer();
		return broker;
	}

}
