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

package org.ardulink.mqtt.camel;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.IOException;

import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.MqttBroker.Builder;
import org.ardulink.mqtt.MqttMain;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
// TODO PF fix test class
public class MqttMainStandaloneIntegrationTest {

	private static final String topic = "myTestTopic";

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

	@Test
	public void clientCanConnectToNewlyStartedBroker() throws Exception {
		MqttMain mqttMain = new MqttMain();
		mqttMain.setConnection("ardulink://mock");
		mqttMain.setStandalone(true);
		mqttMain.setBrokerTopic(topic);

		try {
			mqttMain.connectToMqttBroker();
		} finally {
			mqttMain.close();
		}

	}

	@Test
	public void clientFailsToConnectUsingWrongCredentialsToNewlyStartedBroker()
			throws Exception {
		final String user = "someUser";
		final String password = "secret";
		MqttMain mqttMain = new MqttMain() {
			@Override
			protected Builder createBroker() {
				return super.createBroker().addAuthenication(user,
						password.getBytes());
			}
		};
		mqttMain.setConnection("ardulink://mock");
		mqttMain.setStandalone(true);
		mqttMain.setBrokerTopic(topic);

		mqttMain.setCredentials(user + ":" + "wrongPassword");

		try {
			exceptions.expect(IOException.class);
			exceptions.expectMessage(allOf(containsString("BAD"),
					containsString("USERNAME"), containsString("OR"),
					containsString("PASSWORD")));
			mqttMain.connectToMqttBroker();
		} finally {
			mqttMain.close();
		}
	}

	@Test
	public void clientCanConnectUsingCredentialsToNewlyStartedBroker()
			throws Exception {
		final String user = "someUser";
		final String password = "secret";
		MqttMain mqttMain = new MqttMain() {
			@Override
			protected Builder createBroker() {
				return super.createBroker().addAuthenication(user,
						password.getBytes());
			}
		};
		mqttMain.setConnection("ardulink://mock");
		mqttMain.setStandalone(true);
		mqttMain.setBrokerTopic(topic);

		mqttMain.setCredentials("someUser" + ":" + password);

		try {
			mqttMain.connectToMqttBroker();
		} finally {
			mqttMain.close();
			System.clearProperty(MqttBroker.Builder.propertyName());
		}
	}

}
