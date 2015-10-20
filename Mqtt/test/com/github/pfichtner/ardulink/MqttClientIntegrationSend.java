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

import static com.github.pfichtner.ardulink.util.ProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.ANALOG_PIN_READ;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.DIGITAL_PIN_READ;
import static com.github.pfichtner.ardulink.util.TestUtil.createConnection;
import static com.github.pfichtner.ardulink.util.TestUtil.getField;
import static com.github.pfichtner.ardulink.util.TestUtil.listWithSameOrder;
import static com.github.pfichtner.ardulink.util.TestUtil.set;
import static com.github.pfichtner.ardulink.util.TestUtil.startAsync;
import static com.github.pfichtner.ardulink.util.TestUtil.startBroker;
import static com.github.pfichtner.ardulink.util.TestUtil.toCodepoints;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zu.ardulink.ConnectionContact;
import org.zu.ardulink.Link;
import org.zu.ardulink.connection.Connection;

import com.github.pfichtner.ardulink.util.AnotherMqttClient;
import com.github.pfichtner.ardulink.util.MqttMessageBuilder;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class MqttClientIntegrationSend {

	private static final long TIMEOUT = 10 * 1000;;

	private static final String TOPIC = "foo/bar";

	private static final String LINKNAME = "testlink";

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	private final ConnectionContact connectionContact = new ConnectionContact(
			null);

	private final Connection connection = createConnection(outputStream,
			connectionContact);

	private final Link link = Link.createInstance(LINKNAME, connection);

	{
		// there is an extremely high coupling of ConnectionContact and Link
		// which can not be solved other than injecting the variables through
		// reflection
		set(connectionContact, getField(connectionContact, "link"), link);
		set(link, getField(link, "connectionContact"), connectionContact);

	}

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
	public void tearDown() throws InterruptedException, MqttException {
		this.client.close();
		this.amc.disconnect();
		this.broker.stopServer();
	}

	@Test(timeout = TIMEOUT)
	public void generatesBrokerEventOnDigitalPinChange()
			throws InterruptedException, MqttSecurityException, MqttException,
			IOException {

		int pin = 1;
		this.client.setThrottleMillis(0);
		this.client.setAnalogs();
		this.client.setDigitals(pin);

		startAsync(client);
		simulateArduinoToMqtt(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin)
				.withValue(1));

		tearDown();

		assertThat(this.amc.hasReceived(),
				is(listWithSameOrder(MqttMessageBuilder
						.mqttMessageWithBasicTopic(TOPIC).digitalPin(pin)
						.hasValue(1))));
	}

	@Test(timeout = TIMEOUT)
	public void generatesBrokerEventOnAnalogPinChange()
			throws InterruptedException, MqttSecurityException, MqttException,
			IOException {

		int pin = 1;
		int value = 45;
		this.client.setThrottleMillis(0);
		this.client.setAnalogs(pin);
		this.client.setDigitals();

		startAsync(this.client);
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.withValue(value));

		tearDown();

		assertThat(this.amc.hasReceived(),
				is(listWithSameOrder(MqttMessageBuilder
						.mqttMessageWithBasicTopic(TOPIC).analogPin(pin)
						.hasValue(value))));
	}

	private void simulateArduinoToMqtt(String message) {
		int[] codepoints = toCodepoints(message);
		this.connectionContact.parseInput("someId", codepoints.length,
				codepoints);
	}

}
