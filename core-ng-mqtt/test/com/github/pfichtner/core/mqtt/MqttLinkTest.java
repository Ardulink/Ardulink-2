package com.github.pfichtner.core.mqtt;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.github.pfichtner.ardulink.core.Pin.Type.DIGITAL;
import static com.github.pfichtner.core.mqtt.duplicated.EventMatchers.eventFor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.RuleChain.outerRule;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.ConnectionListener;
import com.github.pfichtner.ardulink.core.events.PinValueChangedEvent;
import com.github.pfichtner.core.mqtt.duplicated.AnotherMqttClient;
import com.github.pfichtner.core.mqtt.duplicated.EventMatchers.PinValueChangedEventMatcher;
import com.github.pfichtner.core.mqtt.duplicated.Message;

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

	private final Broker broker = Broker.newBroker();

	private AnotherMqttClient mqttClient = AnotherMqttClient.newClient(TOPIC);

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public RuleChain chain = outerRule(broker).around(mqttClient);

	@Test
	public void defaultHostIsLocalhostAndLinkHasCreatedWithoutConfiguring()
			throws UnknownHostException, IOException, MqttException {
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
				is(Arrays.asList(new Message(TOPIC + "/A8/value/set", "9"))));
		link.close();
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
			MqttException, MqttPersistenceException, InterruptedException {
		TrackStateConnectionListener connectionListener = new TrackStateConnectionListener();
		link.addConnectionListener(connectionListener);
		assertThat(connectionListener.isConnected(), is(true));
		this.mqttClient.close();

		restartBroker(connectionListener);
		waitForLinkReconnect(connectionListener);

		this.mqttClient = AnotherMqttClient.newClient(TOPIC).connect();
	}

	private MqttLink makeLink(EventCollector eventCollector)
			throws UnknownHostException, IOException, MqttException {
		MqttLinkFactory factory = new MqttLinkFactory();
		MqttLink link = factory.newLink(makeConfig(factory));
		link.addListener(eventCollector);
		return link;
	}

	private MqttLinkConfig makeConfig(MqttLinkFactory factory) {
		MqttLinkConfig config = factory.newLinkConfig();
		config.setTopic(TOPIC);
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
