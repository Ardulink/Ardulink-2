package org.zu.ardulink.gui;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;

public class DummyLinkFactory implements LinkFactory<DummyLinkConfig> {


	@Override
	public String getName() {
		return "dummy";
	}

	@Override
	public Link newLink(DummyLinkConfig config) throws Exception {
		return null;
	}

	@Override
	public DummyLinkConfig newLinkConfig() {
		return new DummyLinkConfig();
	}

}
