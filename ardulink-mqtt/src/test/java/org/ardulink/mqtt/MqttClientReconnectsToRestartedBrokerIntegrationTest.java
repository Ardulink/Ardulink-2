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
package org.ardulink.mqtt;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.mqtt.util.TestUtil.listWithSameOrder;
import static org.ardulink.mqtt.util.TestUtil.startAsync;
import static org.ardulink.mqtt.util.TestUtil.waitUntilIsConnected;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import io.moquette.server.Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.mqtt.util.AnotherMqttClient;
import org.ardulink.mqtt.util.MqttMessageBuilder;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttClientReconnectsToRestartedBrokerIntegrationTest {

	@Rule
	public Timeout timeout = new Timeout(20, SECONDS);

	private static final String TOPIC = "foo/bar";

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	private final Connection connection = new StreamConnection(null,
			outputStream, ArdulinkProtocol2.instance());

	private final ConnectionBasedLink link = new ConnectionBasedLink(
			connection, ArdulinkProtocol2.instance());

	private MqttMain client = new MqttMain() {
		{
			setBrokerTopic(TOPIC);
			setClientId("lnk-" + Thread.currentThread().getId() + "-"
					+ System.currentTimeMillis());
		}

		@Override
		protected Link createLink() {
			return link;
		}
	};

	private Server broker = MqttBroker.builder().startBroker();

	@After
	public void tearDown() throws InterruptedException, IOException {
		client.close();
		link.close();
		if (broker != null) {
			broker.stopServer();
		}
	}

	@Test
	public void clientConnectsAfterBrokerRestarted() throws Exception {
		doNotListenForAnything(client);
		startAsync(client);

		stopBroker();
		assertThat(client.isConnected(), is(false));

		startBroker();
		assertThat(client.isConnected(), is(true));
	}

	@Test
	public void acceptsMessagesWhileNotConnected() throws Exception {
		doNotListenForAnything(client);
		client.setAnalogs(3, 4, 5);
		startAsync(client);

		stopBroker();

		link.fireStateChanged(new DefaultAnalogPinValueChangedEvent(
				analogPin(4), 123));
	}

	@Test
	public void clientContinuesToSendMessagesAfterReconnection()
			throws Exception {
		doNotListenForAnything(client);
		client.setAnalogs(3, 4, 5);
		startAsync(client);

		restartBroker();

		AnotherMqttClient amc = AnotherMqttClient.builder().topic(TOPIC)
				.connect();
		link.fireStateChanged(new DefaultAnalogPinValueChangedEvent(
				analogPin(4), 123));
		assertThat(amc.hasReceived(), is(listWithSameOrder(MqttMessageBuilder
				.mqttMessageWithBasicTopic(TOPIC).analogPin(4).hasValue(123))));
		amc.close();
	}

	private void restartBroker() throws InterruptedException {
		stopBroker();
		startBroker();
	}

	private void startBroker() throws InterruptedException {
		broker = MqttBroker.builder().startBroker();
		waitUntilIsConnected(client, 3, SECONDS);
	}

	private void stopBroker() throws InterruptedException {
		MILLISECONDS.sleep(250);
		broker.stopServer();
		MILLISECONDS.sleep(250);
	}

	private static void doNotListenForAnything(MqttMain client) {
		client.setAnalogs();
		client.setDigitals();
	}

}
