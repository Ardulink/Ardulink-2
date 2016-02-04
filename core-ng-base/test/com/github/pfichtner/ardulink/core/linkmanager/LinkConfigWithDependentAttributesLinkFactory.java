package com.github.pfichtner.ardulink.core.linkmanager;

import java.io.IOException;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol2;

public class LinkConfigWithDependentAttributesLinkFactory implements
		LinkFactory<LinkConfigWithDependentAttributes> {

	public static class H implements Connection {

		public H(LinkConfigWithDependentAttributes config) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void write(byte[] bytes) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void addListener(Listener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public void removeListener(Listener listener) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public String getName() {
		return "dependendAttributes";
	}

	@Override
	public ConnectionBasedLink newLink(LinkConfigWithDependentAttributes config) {
		return new ConnectionBasedLink(new H(config),
				ArdulinkProtocol2.instance());
	}

	@Override
	public LinkConfigWithDependentAttributes newLinkConfig() {
		return new LinkConfigWithDependentAttributes();
	}

}
