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

import static io.moquette.broker.config.IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.ENABLE_TELEMETRY_NAME;
import static io.moquette.broker.config.IConfig.HOST_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.KEY_MANAGER_PASSWORD_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.PORT_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.SSL_PORT_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.WEB_SOCKET_PORT_PROPERTY_NAME;
import static org.ardulink.mqtt.MqttCamelRouteBuilder.DEFAULT_PORT;
import static org.ardulink.mqtt.MqttCamelRouteBuilder.DEFAULT_SSL_PORT;
import static org.ardulink.util.Maps.merge;
import static org.ardulink.util.Maps.toProperties;
import static org.ardulink.util.Maps.valuesToString;
import static org.ardulink.util.Throwables.propagate;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthenticator;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttBroker implements Closeable {

	public static class Builder {

		private IAuthenticator authenticator;
		private String host = "localhost";
		private Integer port;
		private boolean ssl;

		private Builder() {
			super();
		}

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(Integer port) {
			this.port = port;
			return this;
		}

		public Builder useSsl(boolean ssl) {
			this.ssl = ssl;
			return this;
		}

		public Builder addAuthenication(String username, byte[] password) {
			this.authenticator = (i, u, p) -> Objects.equals(username, u) && Arrays.equals(password, p);
			return this;
		}

		public MqttBroker startBroker() {
			return new MqttBroker(this);
		}

		public Map<String, String> properties() {
			return valuesToString(addSsl(Map.of( //
					HOST_PROPERTY_NAME, host, //
					PORT_PROPERTY_NAME, ssl ? 0 : configuredPortOrElse(DEFAULT_PORT), //
					ALLOW_ANONYMOUS_PROPERTY_NAME, authenticator == null, //
					PERSISTENCE_ENABLED_PROPERTY_NAME, false, //
					ENABLE_TELEMETRY_NAME, false //
			)));
		}

		private Map<String, Object> addSsl(Map<String, Object> properties) {
			return ssl //
					? merge(properties, Map.of( //
							SSL_PORT_PROPERTY_NAME, configuredPortOrElse(DEFAULT_SSL_PORT), //
							WEB_SOCKET_PORT_PROPERTY_NAME, 0, //
							KEY_MANAGER_PASSWORD_PROPERTY_NAME, "non-null-value")) //
					: properties;
		}

		private int configuredPortOrElse(int fallback) {
			return port == null ? fallback : port;
		}

	}

	private final Server broker;
	private final IConfig config;

	public static Builder builder() {
		return new Builder();
	}

	public MqttBroker(Builder builder) {
		config = new MemoryConfig(toProperties(builder.properties()));
		broker = startBroker(new Server(), config, builder.authenticator);
	}

	private Server startBroker(Server server, IConfig memoryConfig, IAuthenticator authenticator) {
		try {
			server.startServer(memoryConfig, null, null, authenticator, null);
			return server;
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	public String getHost() {
		return config.getProperty(HOST_PROPERTY_NAME);
	}

	public int getPort() {
		return Integer.parseInt(config.getProperty(PORT_PROPERTY_NAME));
	}

	@Override
	public void close() {
		broker.stopServer();
	}

	public void stop() {
		close();
	}

}
