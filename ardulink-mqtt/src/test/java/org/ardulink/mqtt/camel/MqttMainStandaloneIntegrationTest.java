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

import static org.ardulink.util.ServerSockets.freePort;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.camel.FailedToStartRouteException;
import org.ardulink.mqtt.CommandLineArguments;
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.MqttBroker.Builder;
import org.ardulink.mqtt.MqttMain;
import org.ardulink.util.Strings;
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
@Timeout(10)
class MqttMainStandaloneIntegrationTest {

	private final CommandLineArguments args = args();
	private String brokerUser;
	private String brokerPassword;
	private String clientUser;
	private String clientPassword;

	private static CommandLineArguments args() {
		CommandLineArguments args = new CommandLineArguments();
		args.standalone = true;
		args.brokerTopic = "myTestTopic";
		args.connection = "ardulink://mock";
		return args;
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
		return !Strings.nullOrEmpty(brokerPassword) && brokerPassword != null;
	}

	@Test
	void clientCanConnectToNewlyStartedBroker() throws Exception {
		withBrokerPort(freePort());
		runMain();
	}

	@Test
	void clientCanConnectUsingCredentialsToNewlyStartedBroker() throws Exception {
		String user = "someUser";
		String password = "someSecret";
		withBrokerPort(freePort()).withBrokerUser(user).withBrokerPassword(password).withClientUser(user)
				.withClientPassword(password);
		runMain();
	}

	@Test
	void clientFailsToConnectUsingWrongCredentialsToNewlyStartedBroker() throws Exception {
		String user = "someUser";
		withBrokerPort(freePort()).withBrokerUser(user).withBrokerPassword("theBrokersPassword").withClientUser(user)
				.withClientPassword("notTheBrokersPassword");
//		exceptions.expectMessage("CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD");
		assertThrows(FailedToStartRouteException.class, () -> runMain());
	}

	private void runMain() throws Exception {
		MqttMain mqttMain = new MqttMain(args) {
			@Override
			protected Builder createBroker() {
				Builder builder = super.createBroker();
				if (hasAuthentication()) {
					return builder.addAuthenication(brokerUser, brokerPassword.getBytes());
				}
				return builder;
			}
		};
		mqttMain.connectToMqttBroker();
		mqttMain.close();
		System.clearProperty(MqttBroker.Builder.propertyName());
	}

	@Test
	@Disabled

	// test fails with io.netty.handler.codec.DecoderException:

	void clientCanConnectUsingCredentialsToNewlyStartedSslBroker() throws Exception {
		String user = "someUser";
		String password = "someSecret";
		withSsl().withBrokerPort(freePort()).withBrokerUser(user).withBrokerPassword(password).withClientUser(user)
				.withClientPassword(password);
		runMain();
	}

}
