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
import static io.moquette.BrokerConstants.PORT_PROPERTY_NAME;
import static org.ardulink.util.Throwables.propagate;
import io.moquette.server.Server;
import io.moquette.server.config.MemoryConfig;
import io.moquette.spi.security.IAuthenticator;

import java.io.Closeable;
import java.io.IOException;
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
		private int port = 1883;

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder addAuthenication(String user, byte[] password) {
			System.setProperty(propertyName(), user + ":"
					+ new String(password));
			return this;
		}

		public MqttBroker startBroker() {
			return new MqttBroker(this);
		}

		public static class EnvironmentAuthenticator implements IAuthenticator {
			@Override
			public boolean checkValid(String user, byte[] pass) {
				String userPass = userPass();
				String[] split = userPass.split("\\:");
				return split.length == 2 && split[0].equals(user)
						&& split[1].equals(new String(pass));
			}

		}

		public Properties properties() {
			properties.put(HOST_PROPERTY_NAME, host);
			properties.put(PORT_PROPERTY_NAME, String.valueOf(port));
			String property = userPass();
			if (!Strings.nullOrEmpty(property)) {
				properties.setProperty(AUTHENTICATOR_CLASS_NAME,
						EnvironmentAuthenticator.class.getName());
				properties.setProperty(ALLOW_ANONYMOUS_PROPERTY_NAME,
						Boolean.FALSE.toString());
			}
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

	public static Builder builder() {
		return new Builder();
	}

	public MqttBroker(Builder builder) {
		broker = new Server();
		try {
			broker.startServer(new MemoryConfig(builder.properties()));
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	public void close() {
		broker.stopServer();
	}

	public void stop() {
		close();
	}

}
