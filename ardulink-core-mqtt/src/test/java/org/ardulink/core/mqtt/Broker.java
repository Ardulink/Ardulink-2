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

import static io.moquette.BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME;
import static io.moquette.BrokerConstants.AUTHENTICATOR_CLASS_NAME;
import static io.moquette.BrokerConstants.HOST_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PORT_PROPERTY_NAME;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.MemoryConfig;
import io.moquette.spi.security.IAuthenticator;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.mqtt.duplicated.Message;
import org.ardulink.util.Lists;
import org.ardulink.util.Strings;
import org.junit.rules.ExternalResource;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Broker extends ExternalResource {

	public static class EnvironmentAuthenticator implements IAuthenticator {

		@Override
		public boolean checkValid(String user, byte[] pass) {
			String userPass = userPass();
			String[] split = userPass.split("\\:");
			return split.length == 2 && split[0].equals(user)
					&& split[1].equals(new String(pass));
		}

	}

	private Server mqttServer;
	private String host = "localhost";
	private int port = 1883;
	private final List<InterceptHandler> listeners = Lists.newArrayList();
	private final List<Message> messages = new CopyOnWriteArrayList<Message>();
	private String env2restore;

	private Broker() {
		super();
	}

	public static Broker newBroker() {
		return new Broker();
	}

	public static Broker newBroker(
			Collection<? extends InterceptHandler> listeners) {
		Broker newBroker = newBroker();
		newBroker.listeners.addAll(listeners);
		return newBroker;
	}

	@Override
	protected void before() throws IOException, InterruptedException {
		this.mqttServer = new Server();
		start();
	}

	@Override
	protected void after() {
		stop();
	}

	public void start() throws IOException {
		MemoryConfig memoryConfig = new MemoryConfig(properties());
		this.mqttServer.startServer(memoryConfig, listeners);
	}

	private Properties properties() {
		Properties properties = new Properties();
		properties.put(HOST_PROPERTY_NAME, host);
		properties.put(PORT_PROPERTY_NAME, String.valueOf(port));
		String property = userPass();
		if (!Strings.nullOrEmpty(property)) {
			properties.setProperty(AUTHENTICATOR_CLASS_NAME,
					EnvironmentAuthenticator.class.getName());
			properties.setProperty(ALLOW_ANONYMOUS_PROPERTY_NAME,
					Boolean.FALSE.toString());
		}
		properties.put(PERSISTENT_STORE_PROPERTY_NAME, "");
		return properties;
	}

	private static String userPass() {
		return System.getProperty(propertyName());
	}

	private static String propertyName() {
		return Broker.class.getName() + ".authentication";
	}

	public void stop() {
		this.mqttServer.stopServer();
		if (this.env2restore == null) {
			System.clearProperty(propertyName());
		} else {
			System.setProperty(propertyName(), this.env2restore);
		}
	}

	public Broker recordMessages() {
		listeners.add(new AbstractInterceptHandler() {
			public void onPublish(InterceptPublishMessage message) {
				messages.add(new Message(message.getTopicName(), new String(
						message.getPayload().array())));
			};
		});
		return this;
	}

	public Broker host(String host) {
		this.host = host;
		return this;
	}

	public Broker port(int port) {
		this.port = port;
		return this;
	}

	public int getPort() {
		return port;
	}

	public Broker authentication(String authentication) {
		this.env2restore = System.setProperty(propertyName(), authentication);
		return this;
	}

	public List<Message> getMessages() {
		return Lists.newArrayList(messages);
	}

}
