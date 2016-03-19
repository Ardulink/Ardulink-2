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

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.mqtt.util.TestUtil.startAsync;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import org.dna.mqtt.moquette.server.Server;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.mqtt.util.AnotherMqttClient;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttClientIntegrationReceive {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	private static final String TOPIC = "foo/bar";

	private final Link link = mock(Link.class);

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

	private final Server broker = MqttBroker.builder().startBroker();

	private final AnotherMqttClient amc = AnotherMqttClient.builder()
			.topic(TOPIC).connect();

	@After
	public void tearDown() throws InterruptedException, IOException {
		client.close();
		amc.close();
		broker.stopServer();
	}

	@Test
	public void processesBrokerEventPowerOnDigitalPin() throws Exception {

		int pin = 1;
		boolean value = true;

		doNotListenForAnything(client);
		startAsync(client);
		amc.switchPin(digitalPin(pin), true);

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
		amc.switchPin(analogPin(pin), value);

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
