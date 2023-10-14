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
import static org.ardulink.util.ServerSockets.freePort;
import static org.ardulink.util.Throwables.getCauses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Strings.isNullOrEmpty;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.apache.camel.FailedToStartRouteException;
import org.ardulink.mqtt.CommandLineArguments;
import org.ardulink.mqtt.MqttBroker.Builder;
import org.ardulink.mqtt.MqttMain;
import org.ardulink.testsupport.mock.junit5.MockUri;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Timeout(value = 10, unit = SECONDS)
class MqttMainStandaloneIntegrationTest {

	String someUser = "someUser";
	String somePassword = "somePassword";

	CommandLineArguments args;
	String brokerUser;
	String brokerPassword;

	@BeforeEach
	void setup(@MockUri String mockUri) {
		args = new CommandLineArguments();
		args.standalone = true;
		args.brokerPort = freePort();
		args.brokerTopic = "any/test/topic";
		args.connection = mockUri;
	}

	@Test
	void clientCanConnectToNewlyStartedBroker() throws Exception {
		assertDoesNotThrow(this::runMainAndConnectToBroker);
	}

	@Test
	void clientCanConnectUsingCredentialsToNewlyStartedBroker() throws Exception {
		givenBrokerCredentials(someUser, somePassword);
		givenClientCredentials(someUser, somePassword);
		assertDoesNotThrow(this::runMainAndConnectToBroker);
	}

	@Test
	void clientFailsToConnectUsingWrongCredentialsToNewlyStartedBroker() throws Exception {
		givenBrokerCredentials(someUser, somePassword);
		givenClientCredentials(someUser, not(somePassword));
		assertThatThrownBy(this::runMainAndConnectToBroker).isInstanceOfSatisfying(FailedToStartRouteException.class,
				e -> assertThat(getCauses(e)).anyMatch(MqttSecurityException.class::isInstance));
	}

	@Test
	@Disabled("test fails with Caused by: javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure")
	void clientCanConnectUsingCredentialsToNewlyStartedSslBroker() throws Exception {
		givenSslEnabled(true);
		givenBrokerCredentials(someUser, somePassword);
		givenClientCredentials(someUser, somePassword);
		assertDoesNotThrow(this::runMainAndConnectToBroker);
	}

	private void runMainAndConnectToBroker() throws Exception {
		MqttMain mqttMain = new MqttMain(args) {
			@Override
			protected Builder configureBroker(Builder builder) {
				return hasAuthentication() ? builder.addAuthenication(brokerUser, brokerPassword.getBytes()) : builder;
			}
		};
		mqttMain.connectToMqttBroker();
		mqttMain.close();
	}

	private void givenSslEnabled(boolean state) {
		args.ssl = state;
	}

	private void givenBrokerCredentials(String brokerUser, String brokerPassword) {
		this.brokerUser = brokerUser;
		this.brokerPassword = brokerPassword;
	}

	private void givenClientCredentials(String clientUser, String clientPassword) {
		args.credentials = clientUser + ":" + clientPassword;
	}

	private boolean hasAuthentication() {
		return !isNullOrEmpty(brokerUser) || !isNullOrEmpty(brokerPassword);
	}

	private static String not(String value) {
		return "not" + value;
	}

}
