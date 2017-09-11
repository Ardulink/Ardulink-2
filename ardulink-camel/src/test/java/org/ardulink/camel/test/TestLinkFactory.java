package org.ardulink.camel.test;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkFactory;

public class TestLinkFactory implements LinkFactory<TestLinkConfig> {

	@Override
	public String getName() {
		return "testlink";
	}

	@Override
	public Link newLink(TestLinkConfig config) throws Exception {
		return new TestLink(config);
	}

	@Override
	public TestLinkConfig newLinkConfig() {
		return new TestLinkConfig();
	}

}
