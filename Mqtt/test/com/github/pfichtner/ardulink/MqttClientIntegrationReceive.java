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
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zu.ardulink.Link;

import com.github.pfichtner.ardulink.util.AnotherMqttClient;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class MqttClientIntegrationReceive {

	private static final long TIMEOUT = 10 * 1000;;

	private static final String TOPIC = "foo/bar";

	private final Link link = mock(Link.class);
	{
		when(link.getPortList()).thenReturn(singletonList("/dev/null"));
		when(link.connect("/dev/null", 115200)).thenReturn(true);
		when(link.isConnected()).thenReturn(true);
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
		broker = startBroker();
		amc = new AnotherMqttClient(TOPIC).connect();
	}

	@After
	public void tearDown() throws InterruptedException, MqttException {
		client.close();
		amc.disconnect();
		broker.stopServer();
	}

	@Test(timeout = TIMEOUT)
	public void processesBrokerEventPowerOnDigitalPin()
			throws InterruptedException, MqttSecurityException, MqttException,
			IOException {

		int pin = 1;
		int value = 1;

		doNotListenForAnything(client);
		startAsync(client);
		amc.switchDigitalPin(pin, true);

		tearDown();

		verify(link).getPortList();
		verify(link).connect("/dev/null", 115200);
		verify(link).sendPowerPinSwitch(pin, value);
		verify(link).isConnected();
		verify(link).disconnect();
		verifyNoMoreInteractions(link);
	}

	@Test(timeout = TIMEOUT)
	public void processesBrokerEventPowerOnAnalogPin()
			throws InterruptedException, MqttSecurityException, MqttException,
			IOException {

		int pin = 1;
		int value = 123;

		doNotListenForAnything(client);
		startAsync(client);
		amc.switchAnalogPin(pin, value);

		tearDown();

		verify(link).getPortList();
		verify(link).connect("/dev/null", 115200);
		verify(link).sendPowerPinIntensity(pin, value);
		verify(link).isConnected();
		verify(link).disconnect();
		verifyNoMoreInteractions(link);
	}

	private static void doNotListenForAnything(MqttMain client) {
		client.setAnalogs();
		client.setDigitals();
	}

}
