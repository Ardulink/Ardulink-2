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

package com.github.pfichtner.ardulink;

import java.io.IOException;
import java.util.Properties;

import org.dna.mqtt.moquette.server.Server;

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

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Server startBroker() {
			Server broker = new Server();
			try {
				broker.startServer(properties());
				return broker;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public Properties properties() {
			properties.put("host", host);
			properties.put("port", String.valueOf(port));
			properties.put("password_file", "");
			return properties;
		}

	}

	public static Builder builder() {
		return new Builder();
	}

}
