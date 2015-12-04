package com.github.pfichtner.ardulink.core.linkmanager;

import com.github.pfichtner.ardulink.core.ConnectionBasedLink;

public class DummyLinkFactory implements LinkFactory<DummyLinkConfig> {

	@Override
	public String getName() {
		return "dummyLink";
	}

	@Override
	public ConnectionBasedLink newLink(DummyLinkConfig config) {
		return new ConnectionBasedLink(new DummyConnection(config),
				config.protocol);
	}

	@Override
	public DummyLinkConfig newLinkConfig() {
		return new DummyLinkConfig();
	}

}
