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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.util.URIs;
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
public class MqttWithAuthenticationIntegrationTest {

	private static final String USER = "user";

	private static final String PASSWORD = "pass";

	private static final String TOPIC = "myTopic" + System.currentTimeMillis();

	@Rule
	public Broker broker = Broker.newBroker().authentication(
			USER + ":" + PASSWORD);

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

	@Test
	public void canNotConnectWithoutUserAndPassword() {
		exceptions.expect(RuntimeException.class);
		exceptions.expectMessage(allOf(containsString("BAD"),
				containsString("USERNAME"), containsString("OR"),
				containsString("PASSWORD")));
		LinkManager
				.getInstance()
				.getConfigurer(
						URIs.newURI("ardulink://mqtt?host=localhost&port=1883&topic="
								+ TOPIC)).newLink();
	}

	@Test
	public void canNotConnectWithWrongPassword() {
		exceptions.expect(RuntimeException.class);
		exceptions.expectMessage(allOf(containsString("BAD"),
				containsString("USERNAME"), containsString("OR"),
				containsString("PASSWORD")));
		String wrongPassword = "wrong";
		LinkManager
				.getInstance()
				.getConfigurer(
						URIs.newURI("ardulink://mqtt?host=localhost&port=1883&user="
								+ USER
								+ "&password="
								+ wrongPassword
								+ "&topic=" + TOPIC)).newLink();
	}

	@Test
	public void canConnectUsingUserAndPassword() {
		LinkManager
				.getInstance()
				.getConfigurer(
						URIs.newURI("ardulink://mqtt?host=localhost&port=1883&user="
								+ USER
								+ "&password="
								+ PASSWORD
								+ "&topic="
								+ TOPIC)).newLink();
	}
}
