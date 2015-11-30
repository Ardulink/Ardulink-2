package com.github.pfichtner.ardulink.core.linkmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;
import com.github.pfichtner.ardulink.core.linkmanager.DummyLinkFactory.DummyConnectionConfig;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol;

public class DummyLinkFactory implements LinkFactory<DummyConnectionConfig> {

	public static class DummyConnectionConfig implements LinkConfig {

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
		private List<Listener> listeners = new ArrayList<Listener>();

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
			this.listeners.add(listener);
		}

		@Override
		public void removeListener(Listener listener) {
			this.listeners.remove(listener);
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
	public Link newLink(DummyConnectionConfig config) {
		return new ConnectionBasedLink(new DummyConnection(config),
				ArdulinkProtocol.instance());
	}

	@Override
	public DummyConnectionConfig newLinkConfig() {
		return new DummyConnectionConfig();
	}

}
