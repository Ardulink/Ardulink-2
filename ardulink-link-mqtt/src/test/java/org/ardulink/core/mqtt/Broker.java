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

import static io.moquette.broker.config.IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.ENABLE_TELEMETRY_NAME;
import static io.moquette.broker.config.IConfig.HOST_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.PORT_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.WEB_SOCKET_PORT_PROPERTY_NAME;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toMap;
import static org.ardulink.util.Maps.toProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.mqtt.duplicated.Message;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Broker implements BeforeEachCallback, AfterEachCallback {

	private Server mqttServer;
	private IAuthenticator authenticator;
	private String host = MqttLinkConfig.DEFAULT_HOST;
	private int port = MqttLinkConfig.DEFAULT_PORT;
	private final List<InterceptHandler> listeners = new ArrayList<>();
	private final List<Message> messages = new CopyOnWriteArrayList<>();
	private final List<Message> messagesView = unmodifiableList(messages);

	private Broker() {
		super();
	}

	public static Broker newBroker() {
		return new Broker();
	}

	public static Broker newBroker(Collection<? extends InterceptHandler> listeners) {
		Broker newBroker = newBroker();
		newBroker.listeners.addAll(listeners);
		return newBroker;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws IOException {
		this.mqttServer = new Server();
		start();
	}

	@Override
	public void afterEach(ExtensionContext context) {
		stop();
	}

	public void start() throws IOException {
		this.mqttServer.startServer(memoryConfig(), listeners, null, authenticator, null);
	}

	private MemoryConfig memoryConfig() {
		return new MemoryConfig(properties());
	}

	private Properties properties() {
		return toProperties(toStringString(propertyMap()));
	}

	private static Map<String, String> toStringString(Map<String, Object> in) {
		return in.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> Objects.toString(e.getValue(), null)));
	}

	private Map<String, Object> propertyMap() {
		return Map.of( //
				HOST_PROPERTY_NAME, host, //
				PORT_PROPERTY_NAME, port, //
				PERSISTENCE_ENABLED_PROPERTY_NAME, Boolean.FALSE, //
				ENABLE_TELEMETRY_NAME, Boolean.FALSE, //
				WEB_SOCKET_PORT_PROPERTY_NAME, 0, //
				ALLOW_ANONYMOUS_PROPERTY_NAME, isAnonymousLoginAllowed() //
		);
	}

	private boolean isAnonymousLoginAllowed() {
		return this.authenticator == null;
	}

	public void stop() {
		this.mqttServer.stopServer();
	}

	public Broker recordMessages() {
		listeners.add(new AbstractInterceptHandler() {
			@Override
			public String getID() {
				return getClass().getName();
			}

			@Override
			public void onPublish(InterceptPublishMessage message) {
				messages.add(new Message(message.getTopicName(), new String(message.getPayload().array())));
			}

			@Override
			public void onSessionLoopError(Throwable error) {
				throw new IllegalStateException("not implemented");
			}
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

	public Broker authentication(String username, byte[] password) {
		this.authenticator = (c, u, p) -> Objects.equals(username, u) && Arrays.equals(password, p);
		return this;
	}

	public int port() {
		return port;
	}

	public List<Message> getMessages() {
		return messagesView;
	}

}
