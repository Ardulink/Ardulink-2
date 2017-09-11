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

import java.io.IOException;

import org.apache.camel.FailedToCreateProducerException;
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.MqttBroker.Builder;
import org.ardulink.mqtt.MqttMain;
import org.ardulink.util.Strings;
import org.junit.After;
import org.junit.Ignore;
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
public class MqttMainStandaloneIntegrationTest {

	private static final class MqttMainForTest extends MqttMain {

		private String brokerUser;
		private String brokerPassword;
		private String clientUser;
		private String clientPassword;
		private Builder createBroker;

		public MqttMainForTest() {
			setStandalone(true);
			setBrokerTopic(topic);
			setConnection("ardulink://mock");
		}

		private MqttMainForTest withBrokerPort(int port) {
			setBrokerPort(port);
			return this;
		}

		public MqttMainForTest withBrokerUser(String brokerUser) {
			this.brokerUser = brokerUser;
			return this;
		}

		public MqttMainForTest withSsl() {
			setSsl(true);
			return this;
		}

		public MqttMainForTest withBrokerPassword(String brokerPassword) {
			this.brokerPassword = brokerPassword;
			return this;
		}

		public MqttMainForTest withClientUser(String clientUser) {
			this.clientUser = clientUser;
			return updateCredentials();
		}

		public MqttMainForTest withClientPassword(String clientPassword) {
			this.clientPassword = clientPassword;
			return updateCredentials();
		}

		private MqttMainForTest updateCredentials() {
			setCredentials(clientUser + ":" + clientPassword);
			return this;
		}

		@Override
		protected Builder createBroker() {
			this.createBroker = super.createBroker();
			return hasAuthentication() ? this.createBroker.addAuthenication(
					brokerUser, brokerPassword.getBytes()) : this.createBroker;
		}

		private boolean hasAuthentication() {
			return !Strings.nullOrEmpty(brokerPassword)
					&& brokerPassword != null;
		}
	}

	private static final String topic = "myTestTopic";

	@Rule
	public Timeout timeout = new Timeout(10, SECONDS);

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

	private MqttMainForTest sut;

	@After
	public void tearDown() throws IOException {
		sut.close();
		System.clearProperty(MqttBroker.Builder.propertyName());
	}

	private MqttMainForTest mqttMain() {
		return new MqttMainForTest();
	}

	@Test
	public void clientCanConnectToNewlyStartedBroker() throws Exception {
		sut = mqttMain().withBrokerPort(freePort());
		sut.connectToMqttBroker();
	}

	@Test
	public void clientCanConnectUsingCredentialsToNewlyStartedBroker()
			throws Exception {
		String user = "someUser";
		String password = "someSecret";
		sut = mqttMain().withBrokerPort(freePort()).withBrokerUser(user)
				.withBrokerPassword(password).withClientUser(user)
				.withClientPassword(password);
		sut.connectToMqttBroker();
	}

	@Test
	public void clientFailsToConnectUsingWrongCredentialsToNewlyStartedBroker()
			throws Exception {
		String user = "someUser";
		sut = mqttMain().withBrokerPort(freePort()).withBrokerUser(user)
				.withBrokerPassword("theBrokersPassword").withClientUser(user)
				.withClientPassword("notTheBrokersPassword");
		exceptions.expect(FailedToCreateProducerException.class);
		exceptions.expectMessage("CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD");
		sut.connectToMqttBroker();
	}

	@Test
	@Ignore
	// fails because of bug? in moquette?
	// io.netty.handler.codec.DecoderException:
	// javax.net.ssl.SSLHandshakeException: no cipher suites in common
	public void clientCanConnectUsingCredentialsToNewlyStartedSslBroker()
			throws Exception {
		String user = "someUser";
		String password = "someSecret";
		sut = mqttMain().withSsl().withBrokerPort(freePort())
				.withBrokerUser(user).withBrokerPassword(password)
				.withClientUser(user).withClientPassword(password);
		sut.connectToMqttBroker();
	}

}
