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
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.mqtt.util.TestUtil.startAsync;
import static org.ardulink.util.URIs.newURI;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import io.moquette.server.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.mqtt.util.AnotherMqttClient;
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
public class MqttClientReceiveIntegrationTest {

	@Rule
	public Timeout timeout = new Timeout(10, SECONDS);

	private static final String TOPIC = "foo/bar";

	private final Link link = Links.getLink(newURI("ardulink://mock"));

	private MqttMain mqttMain = new MqttMain() {
		{
			setBrokerTopic(TOPIC);
			setClientId("lnk-" + Thread.currentThread().getId() + "-"
					+ System.currentTimeMillis());
		}

		protected Link createLink() {
			return link;
		};
	};

	private final Server broker = MqttBroker.builder().startBroker();

	private final AnotherMqttClient amc = AnotherMqttClient.builder()
			.topic(TOPIC).connect();

	@After
	public void tearDown() throws InterruptedException, IOException {
		TimeUnit.MILLISECONDS.sleep(250);
		mqttMain.close();
		amc.close();
		broker.stopServer();
	}

	@Test
	public void processesBrokerEventPowerOnDigitalPin() throws Exception {

		int pin = 1;
		boolean value = true;

		doNotListenForAnything(mqttMain);
		startAsync(mqttMain);
		amc.switchPin(digitalPin(pin), true);

		tearDown();

		Link mock = getMock(link);
		verify(mock).switchDigitalPin(digitalPin(pin), value);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private Link getMock(Link link) {
		return ((LinkDelegate) link).getDelegate();
	}

	@Test
	public void processesBrokerEventPowerOnAnalogPin() throws Exception {

		int pin = 1;
		int value = 123;

		doNotListenForAnything(mqttMain);
		startAsync(mqttMain);
		amc.switchPin(analogPin(pin), value);

		tearDown();

		Link mock = getMock(link);
		verify(mock).switchAnalogPin(analogPin(pin), value);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private static void doNotListenForAnything(MqttMain client) {
		client.setAnalogs();
		client.setDigitals();
	}

}
