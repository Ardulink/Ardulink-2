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
package com.github.pfichtner.ardulink;

import static com.github.pfichtner.ardulink.util.TestUtil.startAsync;
import static com.github.pfichtner.ardulink.util.TestUtil.startBroker;
import static com.github.pfichtner.ardulink.util.TestUtil.waitUntilIsConnected;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class MqttClientReconnectsToRestartedBrokerIntegrationTest {

	@Rule
	public Timeout timeout = new Timeout(10, SECONDS);;

	private static final String TOPIC = "foo/bar";

	private final Link link = mock(Link.class);

	private MqttMain client = new MqttMain() {
		{
			setBrokerTopic(TOPIC);
		}

		@Override
		protected Link createLink() {
			return link;
		}
	};

	private Server broker;

	@Before
	public void setup() throws IOException, InterruptedException,
			MqttSecurityException, MqttException {
		broker = startBroker();
	}

	@After
	public void tearDown() throws InterruptedException, MqttException, IOException {
		if (client.isConnected()) {
			client.close();
		}
		if (broker != null) {
			broker.stopServer();
		}
	}

	@Test
	public void clientConnectsWhenAfterBrokerRestartet()
			throws Exception {

		doNotListenForAnything(client);
		startAsync(client);

		MILLISECONDS.sleep(250);
		broker.stopServer();
		MILLISECONDS.sleep(250);
		assertThat(client.isConnected(), is(false));
		
		broker = startBroker();
		waitUntilIsConnected(client, 3, SECONDS);
		assertThat(client.isConnected(), is(true));

		tearDown();
	}

	private static void doNotListenForAnything(MqttMain client) {
		client.setAnalogs();
		client.setDigitals();
	}

}
