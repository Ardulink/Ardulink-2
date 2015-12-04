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

import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.github.pfichtner.ardulink.util.TestUtil.startAsync;
import static com.github.pfichtner.ardulink.util.TestUtil.startBroker;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.util.AnotherMqttClient;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class MqttClientIntegrationReceive {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

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

	private AnotherMqttClient amc;

	@Before
	public void setup() throws IOException, InterruptedException,
			MqttSecurityException, MqttException {
		broker = startBroker();
		amc = new AnotherMqttClient(TOPIC).connect();
	}

	@After
	public void tearDown() throws InterruptedException, MqttException,
			IOException {
		client.close();
		amc.disconnect();
		broker.stopServer();
	}

	@Test
	public void processesBrokerEventPowerOnDigitalPin() throws Exception {

		int pin = 1;
		boolean value = true;

		doNotListenForAnything(client);
		startAsync(client);
		amc.switchDigitalPin(pin, true);

		tearDown();

		verify(link).switchDigitalPin(digitalPin(pin), value);
		verify(link).close();
		verifyNoMoreInteractions(link);
	}

	@Test
	public void processesBrokerEventPowerOnAnalogPin() throws Exception {

		int pin = 1;
		int value = 123;

		doNotListenForAnything(client);
		startAsync(client);
		amc.switchAnalogPin(pin, value);

		tearDown();

		verify(link).switchAnalogPin(Pin.analogPin(pin), value);
		verify(link).close();
		verifyNoMoreInteractions(link);
	}

	private static void doNotListenForAnything(MqttMain client) {
		client.setAnalogs();
		client.setDigitals();
	}

}
