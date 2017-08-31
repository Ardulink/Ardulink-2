package org.ardulink.core.virtual;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkFactory;

public class VirtualLinkFactory implements LinkFactory<VirtualLinkConfig> {

	@Override
	public String getName() {
		return "virtual";
	}

	@Override
	public Link newLink(VirtualLinkConfig config) throws Exception {
		return new VirtualLink(config);
	}

	@Override
	public VirtualLinkConfig newLinkConfig() {
		return new VirtualLinkConfig();
	}

}
