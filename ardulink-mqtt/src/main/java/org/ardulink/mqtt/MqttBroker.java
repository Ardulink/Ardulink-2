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

import static io.moquette.BrokerConstants.HOST_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PASSWORD_FILE_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PORT_PROPERTY_NAME;
import static org.ardulink.util.Throwables.propagate;
import io.moquette.server.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.ardulink.util.Strings;
import org.ardulink.util.Throwables;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttBroker {

	public static class Builder {

		private final Properties properties = new Properties();
		private String host = "localhost";
		private int port = 1883;
		private String passwordFile = "";

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder addAuthenication(String user, byte[] password) {
			File file;
			try {
				if (Strings.nullOrEmpty(passwordFile)) {
					file = File.createTempFile("mqttauth", ".tmp");
					passwordFile = file.getAbsoluteFile().toString();
				} else {
					file = new File(passwordFile);
				}
				file.deleteOnExit();
				BufferedWriter writer = new BufferedWriter(new FileWriter(file,
						true));

				try {
					writer.write(user + ":" + sha256(password));
					writer.newLine();
				} finally {
					writer.close();
				}
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
			return this;
		}

		private static String sha256(byte[] password) {
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(password);
				StringBuilder hexString = new StringBuilder(hash.length * 2);
				for (int i = 0; i < hash.length; i++) {
					String hex = Integer.toHexString(0xff & hash[i]);
					if (hex.length() == 1) {
						hexString.append('0');
					}
					hexString.append(hex);
				}
				return hexString.toString();
			} catch (NoSuchAlgorithmException e) {
				throw Throwables.propagate(e);
			}
		}

		public Server startBroker() {
			Server broker = new Server();
			try {
				broker.startServer(properties());
				return broker;
			} catch (IOException e) {
				throw propagate(e);
			}
		}

		public Properties properties() {
			properties.put(HOST_PROPERTY_NAME, host);
			properties.put(PORT_PROPERTY_NAME, String.valueOf(port));
			properties.put(PASSWORD_FILE_PROPERTY_NAME, passwordFile);
			return properties;
		}

	}

	public static Builder builder() {
		return new Builder();
	}

}
