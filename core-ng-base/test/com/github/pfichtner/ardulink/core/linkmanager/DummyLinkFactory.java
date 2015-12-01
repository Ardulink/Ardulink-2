package com.github.pfichtner.ardulink.core.linkmanager;

import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.Link;

public class DummyLinkFactory implements LinkFactory<DummyLinkConfig> {

	@Override
	public String getName() {
		return "dummyLink";
	}

	@Override
	public Link newLink(DummyLinkConfig config) {
		return new ConnectionBasedLink(new DummyConnection(config),
				config.protocol);
	}

	@Override
	public DummyLinkConfig newLinkConfig() {
		return new DummyLinkConfig();
	}

}
