package com.github.pfichtner.core.raspi;

import static com.github.pfichtner.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;

public class PiLinkFactory implements LinkFactory<LinkConfig> {

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
		return NO_ATTRIBUTES;
	}

}
