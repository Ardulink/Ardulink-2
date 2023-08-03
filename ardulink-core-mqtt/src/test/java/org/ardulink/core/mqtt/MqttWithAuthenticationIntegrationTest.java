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

package org.ardulink.core.mqtt;

import static org.ardulink.core.mqtt.Broker.newBroker;
import static org.ardulink.util.ServerSockets.freePort;
import static org.ardulink.util.Throwables.getRootCause;
import static org.ardulink.util.URIs.newURI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_FAILED_AUTHENTICATION;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.ardulink.core.linkmanager.LinkManager;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Timeout(30)
class MqttWithAuthenticationIntegrationTest {

	static final String USER = "user";

	static final String PASSWORD = "pass";

	static final String TOPIC = "myTopic" + System.currentTimeMillis();

	@RegisterExtension
	Broker broker = newBroker().port(freePort()).authentication(USER, PASSWORD.getBytes());

	@Test
	void canNotConnectWithoutUserAndPassword() {
		assertIsAuthError(createLinkAndCatchRTE(newURI(mqttBase())));
	}

	@Test
	void canNotConnectWithWrongPassword() {
		assertIsAuthError(
				createLinkAndCatchRTE(newURI(mqttBase() + "&user=" + USER + "&password=" + "anyWrongPassword")));
	}

	@Test
	void canConnectUsingUserAndPassword() {
		createLink(newURI(mqttBase() + "&user=" + USER + "&password=" + PASSWORD));
	}

	private String mqttBase() {
		return "ardulink://mqtt?port=" + broker.port() + "&topic=" + TOPIC;
	}

	private void assertIsAuthError(Exception exception) {
		assertThat(getRootCause(exception)).isInstanceOfSatisfying(MqttException.class,
				e -> assertThat(e.getReasonCode()).isEqualTo(REASON_CODE_FAILED_AUTHENTICATION));
	}

	private static RuntimeException createLinkAndCatchRTE(URI uri) {
		return assertThrows(RuntimeException.class, () -> createLink(uri));
	}

	private static void createLink(URI uri) {
		LinkManager.getInstance().getConfigurer(uri).newLink();
	}

}
