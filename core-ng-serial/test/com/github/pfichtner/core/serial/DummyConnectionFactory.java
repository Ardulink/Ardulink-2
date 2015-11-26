package com.github.pfichtner.core.serial;

import java.io.IOException;

import com.github.pfichtner.Connection;
import com.github.pfichtner.ardulink.core.ConnectionConfig;
import com.github.pfichtner.ardulink.core.ConnectionFactory;
import com.github.pfichtner.core.serial.DummyConnectionFactory.DummyConnectionConfig;

public class DummyConnectionFactory implements
		ConnectionFactory<DummyConnectionConfig> {

	public static class DummyConnectionConfig implements ConnectionConfig {
		private String port;
		private int speed;

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public int getSpeed() {
			return speed;
		}

		public void setSpeed(int speed) {
			this.speed = speed;
		}

	}

	public static class DummyConnection implements Connection {

		private final DummyConnectionConfig config;

		public DummyConnection(DummyConnectionConfig config) {
			this.config = config;
		}

		@Override
		public void close() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void write(byte[] bytes) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addListener(Listener listener) {
			throw new UnsupportedOperationException();
		}

		public DummyConnectionConfig getConfig() {
			return config;
		}

	}

	@Override
	public String getName() {
		return "dummy";
	}

	@Override
	public Connection newConnection(DummyConnectionConfig config) {
		return new DummyConnection(config);
	}

	@Override
	public DummyConnectionConfig newConnectionConfig() {
		return new DummyConnectionConfig();
	}

}
