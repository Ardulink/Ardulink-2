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
import static io.moquette.broker.config.IConfig.DATA_PATH_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.HOST_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.KEY_MANAGER_PASSWORD_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.PORT_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.SSL_PORT_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.WEB_SOCKET_PORT_PROPERTY_NAME;
import static org.ardulink.util.Throwables.propagate;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

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

		private final Properties properties = new Properties();
		private IAuthenticator authenticator;
		private String host = "localhost";
		private Integer port;
		private boolean ssl;

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(Integer port) {
			this.port = port;
			return this;
		}

		public Builder port(int port) {
			return port(Integer.valueOf(port));
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

		public Properties properties() {
			String sPort = String.valueOf(port == null ? defaultPort() : port.intValue());
			properties.put(HOST_PROPERTY_NAME, host);
			properties.put(PORT_PROPERTY_NAME, sPort);
			if (ssl) {
				properties.put(PORT_PROPERTY_NAME, 0);
				properties.put(SSL_PORT_PROPERTY_NAME, sPort);
				properties.put(WEB_SOCKET_PORT_PROPERTY_NAME, 0);

				properties.put(KEY_MANAGER_PASSWORD_PROPERTY_NAME, "non-null-value");
				// properties.put(KEY_STORE_PASSWORD_PROPERTY_NAME, password);
				// properties.put(JKS_PATH_PROPERTY_NAME, keystoreFilename);
			}
			if (this.authenticator != null) {
				properties.setProperty(ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.FALSE.toString());
			}
			properties.put(DATA_PATH_PROPERTY_NAME, "");
			return properties;
		}

		private int defaultPort() {
			return ssl ? MqttCamelRouteBuilder.DEFAULT_SSL_PORT : MqttCamelRouteBuilder.DEFAULT_PORT;
		}

	}

	private final Server broker;
	private final IConfig config;

	public static Builder builder() {
		return new Builder();
	}

	public MqttBroker(Builder builder) {
		config = new MemoryConfig(builder.properties());
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
