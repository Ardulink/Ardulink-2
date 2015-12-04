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

import static com.github.pfichtner.ardulink.util.TestUtil.analogPinChanged;
import static com.github.pfichtner.ardulink.util.TestUtil.digitalPinChanged;
import static com.github.pfichtner.ardulink.util.TestUtil.listWithSameOrder;
import static com.github.pfichtner.ardulink.util.TestUtil.startAsync;
import static com.github.pfichtner.ardulink.util.TestUtil.startBroker;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol;
import com.github.pfichtner.ardulink.util.AnotherMqttClient;
import com.github.pfichtner.ardulink.util.MqttMessageBuilder;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class MqttClientIntegrationSend {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	private static final String TOPIC = "foo/bar";

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	private final Connection connection = new StreamConnection(null,
			outputStream);

	private final ConnectionBasedLink link = new ConnectionBasedLink(
			connection, ArdulinkProtocol.instance());

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
	private AnotherMqttClient amc;

	@Before
	public void setup() throws IOException, InterruptedException,
			MqttSecurityException, MqttException {
		this.broker = startBroker();
		this.amc = new AnotherMqttClient(TOPIC).connect();
	}

	@After
	public void tearDown() throws InterruptedException, MqttException, IOException {
		this.client.close();
		this.amc.disconnect();
		this.broker.stopServer();
	}

	@Test
	public void generatesBrokerEventOnDigitalPinChange()
			throws Exception {

		int pin = 1;
		this.client.setThrottleMillis(0);
		this.client.setAnalogs();
		this.client.setDigitals(pin);

		startAsync(client);
		link.fireStateChanged(digitalPinChanged(pin, true));

		tearDown();

		assertThat(this.amc.hasReceived(),
				is(listWithSameOrder(MqttMessageBuilder
						.mqttMessageWithBasicTopic(TOPIC).digitalPin(pin)
						.hasState(true))));
	}

	@Test
	public void generatesBrokerEventOnAnalogPinChange() throws Exception {

		int pin = 1;
		int value = 45;
		this.client.setThrottleMillis(0);
		this.client.setAnalogs(pin);
		this.client.setDigitals();

		startAsync(this.client);
		link.fireStateChanged(analogPinChanged(pin, value));

		tearDown();

		assertThat(this.amc.hasReceived(),
				is(listWithSameOrder(MqttMessageBuilder
						.mqttMessageWithBasicTopic(TOPIC).analogPin(pin)
						.hasValue(value))));
	}


}
