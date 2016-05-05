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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.mqtt.util.TestUtil.analogPinChanged;
import static org.ardulink.mqtt.util.TestUtil.digitalPinChanged;
import static org.ardulink.mqtt.util.TestUtil.listWithSameOrder;
import static org.ardulink.mqtt.util.TestUtil.startAsync;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import io.moquette.server.Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
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
public class MqttClientSendIntegrationTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	private static final String TOPIC = "foo/bar";

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	private final Connection connection = new StreamConnection(null,
			outputStream, ArdulinkProtocol2.instance());

	private final ConnectionBasedLink link = new ConnectionBasedLink(
			connection, ArdulinkProtocol2.instance());

	private MqttMain client = new MqttMain() {
		{
			setBrokerTopic(TOPIC);
		}

		@Override
		protected Link createLink() {
			return link;
		}
	};

	private final Server broker = MqttBroker.builder().startBroker();

	private final AnotherMqttClient amc = AnotherMqttClient.builder()
			.topic(TOPIC).connect();

	@After
	public void tearDown() throws InterruptedException, IOException {
		this.client.close();
		this.amc.close();
		this.broker.stopServer();
	}

	@Test
	public void generatesBrokerEventOnDigitalPinChange() throws Exception {

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
