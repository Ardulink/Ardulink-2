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
import static org.ardulink.util.Strings.nullOrEmpty;
import static org.ardulink.util.Throwables.getCauses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

	private final CommandLineArguments args = new CommandLineArguments();
	private String brokerUser;
	private String brokerPassword;
	private String clientUser;
	private String clientPassword;

	@BeforeEach
	void setup(@MockUri String mockUri) {
		args.standalone = true;
		args.brokerTopic = "myTestTopic";
		args.connection = mockUri;
		withBrokerPort(freePort());
	}

	private MqttMainStandaloneIntegrationTest withBrokerPort(int port) {
		args.brokerPort = port;
		return this;
	}

	public MqttMainStandaloneIntegrationTest withBrokerUser(String brokerUser) {
		this.brokerUser = brokerUser;
		return this;
	}

	public MqttMainStandaloneIntegrationTest withSsl() {
		args.ssl = true;
		return this;
	}

	public MqttMainStandaloneIntegrationTest withBrokerPassword(String brokerPassword) {
		this.brokerPassword = brokerPassword;
		return this;
	}

	public MqttMainStandaloneIntegrationTest withClientUser(String clientUser) {
		this.clientUser = clientUser;
		return updateCredentials();
	}

	public MqttMainStandaloneIntegrationTest withClientPassword(String clientPassword) {
		this.clientPassword = clientPassword;
		return updateCredentials();
	}

	private MqttMainStandaloneIntegrationTest updateCredentials() {
		args.credentials = clientUser + ":" + clientPassword;
		return this;
	}

	private boolean hasAuthentication() {
		return !nullOrEmpty(brokerPassword) && brokerPassword != null;
	}

	@Test
	void clientCanConnectToNewlyStartedBroker() throws Exception {
		assertDoesNotThrow(this::runMain);
	}

	@Test
	void clientCanConnectUsingCredentialsToNewlyStartedBroker() throws Exception {
		withBrokerUser(someUser).withBrokerPassword(somePassword);
		withClientUser(someUser).withClientPassword(somePassword);
		assertDoesNotThrow(this::runMain);
	}

	@Test
	void clientFailsToConnectUsingWrongCredentialsToNewlyStartedBroker() throws Exception {
		withBrokerUser(someUser).withBrokerPassword(somePassword);
		withClientUser(someUser).withClientPassword(not(somePassword));
		Exception exception = assertThrows(FailedToStartRouteException.class, () -> runMain());
		assertThat(getCauses(exception).anyMatch(MqttSecurityException.class::isInstance)).isTrue();
	}

	@Test
	@Disabled("test fails with Caused by: javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure")
	void clientCanConnectUsingCredentialsToNewlyStartedSslBroker() throws Exception {
		withSsl();
		withBrokerUser(someUser).withBrokerPassword(somePassword);
		withClientUser(someUser).withClientPassword(somePassword);
		assertDoesNotThrow(this::runMain);
	}

	private void runMain() throws Exception {
		MqttMain mqttMain = new MqttMain(args) {
			@Override
			protected Builder configureBroker(Builder builder) {
				return hasAuthentication() ? builder.addAuthenication(brokerUser, brokerPassword.getBytes()) : builder;
			}
		};
		mqttMain.connectToMqttBroker();
		mqttMain.close();
	}

	private static String not(String value) {
		return "not" + value;
	}

}
