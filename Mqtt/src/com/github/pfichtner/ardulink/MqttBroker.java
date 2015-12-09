package com.github.pfichtner.ardulink;

import java.io.IOException;
import java.util.Properties;

import org.dna.mqtt.moquette.server.Server;

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
