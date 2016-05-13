package org.ardulink.core.virtual;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;

public class VirtualLinkFactory implements LinkFactory<LinkConfig> {

	@Override
	public String getName() {
		return "virtual";
	}

	@Override
	public Link newLink(LinkConfig config) throws Exception {
		return new VirtualLink(config);
	}

	@Override
	public LinkConfig newLinkConfig() {
		return LinkConfig.NO_ATTRIBUTES;
	}

}
