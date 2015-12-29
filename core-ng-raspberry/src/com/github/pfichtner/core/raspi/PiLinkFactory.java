package com.github.pfichtner.core.raspi;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;

public class PiLinkFactory implements LinkFactory<LinkConfig> {

	private static final LinkConfig NO_ARG = new LinkConfig() {
	};

	@Override
	public String getName() {
		return "raspberry";
	}

	@Override
	public Link newLink(LinkConfig config) {
		return new PiLink();
	}

	@Override
	public LinkConfig newLinkConfig() {
		return NO_ARG;
	}

}
