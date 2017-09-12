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

import static io.moquette.BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME;
import static io.moquette.BrokerConstants.AUTHENTICATOR_CLASS_NAME;
import static io.moquette.BrokerConstants.HOST_PROPERTY_NAME;
import static io.moquette.BrokerConstants.JKS_PATH_PROPERTY_NAME;
import static io.moquette.BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME;
import static io.moquette.BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PORT_PROPERTY_NAME;
import static io.moquette.BrokerConstants.SSL_PORT_PROPERTY_NAME;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Throwables.propagate;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import io.moquette.spi.security.IAuthenticator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.ardulink.util.Strings;

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
		private String host = "localhost";
		private Integer port;
		private boolean ssl;

		public Builder host(final String host) {
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

		public Builder addAuthenication(final String user, final byte[] password) {
			System.setProperty(propertyName(), user + ":"
					+ new String(password));
			return this;
		}

		public MqttBroker startBroker() {
			return new MqttBroker(this);
		}

		public static class EnvironmentAuthenticator implements IAuthenticator {

			private final String user;
			private final byte[] pass;

			public EnvironmentAuthenticator() {
				this(userPass());
			}

			public EnvironmentAuthenticator(String userPass) {
				String[] split = userPass.split("\\:");
				checkState(
						split.length == 2,
						"Could not split %s into user:password using separator ':'",
						userPass);
				this.user = split[0];
				this.pass = split[1].getBytes();
			}

			@Override
			public boolean checkValid(String username, byte[] password) {
				return this.user.equals(username)
						&& Arrays.equals(this.pass, password);
			}

			public boolean checkValid(String clientId, String username,
					byte[] password) {
				return checkValid(username, password);
			}

		}

		public Properties properties() {
			String sPort = String.valueOf(port == null ? (ssl ? 8883 : 1883)
					: port.intValue());
			properties.put(HOST_PROPERTY_NAME, host);
			properties.put(PORT_PROPERTY_NAME, sPort);
			if (ssl) {
				properties.put(SSL_PORT_PROPERTY_NAME, sPort);
				properties.put(KEY_STORE_PASSWORD_PROPERTY_NAME, "passw0rdsrv");

				properties.put(JKS_PATH_PROPERTY_NAME, "just-a-non-null-value");
				properties.put(KEY_MANAGER_PASSWORD_PROPERTY_NAME,
						"just-a-non-null-value");
			}
			final String property = userPass();
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

		public static String propertyName() {
			return MqttBroker.class.getName() + ".authentication";
		}

	}

	private final Server broker;
	private final IConfig config;

	public static Builder builder() {
		return new Builder();
	}

	public MqttBroker(final Builder builder) {
		config = new MemoryConfig(builder.properties());
		broker = startBroker(new Server(), config);
	}

	private Server startBroker(Server server, IConfig memoryConfig) {
		try {
			server.startServer(memoryConfig);
			return server;
		} catch (final IOException e) {
			throw propagate(e);
		}
	}

	public int getPort() {
		return Integer.parseInt(config.getProperty(PORT_PROPERTY_NAME));
	}

	public void close() {
		broker.stopServer();
	}

	public void stop() {
		close();
	}

}
