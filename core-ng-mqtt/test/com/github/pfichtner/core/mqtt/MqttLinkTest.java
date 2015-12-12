package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.github.pfichtner.ardulink.core.Pin.Type.DIGITAL;
import static com.github.pfichtner.core.mqtt.duplicated.EventMatchers.eventFor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.RuleChain.outerRule;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.ConnectionListener;
import com.github.pfichtner.core.mqtt.duplicated.AnotherMqttClient;

public class MqttLinkTest {

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

	private final Broker broker = new Broker();

	private final AnotherMqttClient mqttClient = new AnotherMqttClient(TOPIC);

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public RuleChain chain = outerRule(broker).around(mqttClient);

	@Test
	public void defaultHostIsLocalhostAndLinkHasCreatedWithoutConfiguring()
			throws UnknownHostException, IOException, MqttException {
		MqttLinkFactory factory = new MqttLinkFactory();
		MqttLinkConfig config = factory.newLinkConfig();
		String host = config.getHost();
		assertThat(host, is("localhost"));
		MqttLink link = factory.newLink(config);
		assertThat(link, notNullValue());
		link.close();
	}

	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void testReconnect() throws Exception {
		MqttLinkFactory factory = new MqttLinkFactory();
		MqttLinkConfig config = factory.newLinkConfig();
		config.setTopic(TOPIC);
		MqttLink link = factory.newLink(config);

		TrackStateConnectionListener connectionListener = new TrackStateConnectionListener();
		link.addConnectionListener(connectionListener);
		assertThat(connectionListener.isConnected(), is(true));

		EventCollector eventCollector = new EventCollector();
		link.addListener(eventCollector);

		mqttClient.switchPin(digitalPin(1), true);
		assertThat(eventCollector.events(DIGITAL),
				hasItems(eventFor(digitalPin(1)).withValue(true)));
		this.mqttClient.close();

		restartBroker(connectionListener);
		waitForLinkRecoonect(connectionListener);

		new AnotherMqttClient(TOPIC).connect().switchPin(digitalPin(2), true);

		assertThat(
				eventCollector.events(DIGITAL),
				hasItems(eventFor(digitalPin(1)).withValue(true),
						eventFor(digitalPin(2)).withValue(true)));
		link.close();
	}

	public void waitForLinkRecoonect(
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

}
