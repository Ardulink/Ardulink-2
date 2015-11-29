package com.github.pfichtner.ardulink.core.connectionmanager;

import java.io.IOException;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionConfig;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionFactory;
import com.github.pfichtner.ardulink.core.connectionmanager.DummyConnectionFactory.DummyConnectionConfig;

public class DummyConnectionFactory implements
		ConnectionFactory<DummyConnectionConfig> {

	public static class DummyConnectionConfig implements ConnectionConfig {

		public String a;
		public int b;
		@Named("c")
		public String c;

		@Named("a")
		public void setPort(String a) {
			this.a = a;
		}

		@Named("b")
		public void theNameOfTheSetterDoesNotMatter(int b) {
			this.b = b;
		}

		@PossibleValueFor("a")
		public String[] possibleValuesForAtttribute_A() {
			return new String[] { "aVal1", "aVal2" };
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
		
		@Override
		public void removeListener(Listener listener) {
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
