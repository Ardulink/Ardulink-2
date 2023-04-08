package org.ardulink.core.linkmanager;

import static org.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;
import static org.mockito.Mockito.mock;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkFactory.Alias;

@Alias({ "aliasLinkAlias", AliasUsingLinkFactory.ALREADY_TAKEN_NAME })
public class AliasUsingLinkFactory implements LinkFactory<LinkConfig> {

	protected static final String ALREADY_TAKEN_NAME = DummyLinkFactory.DUMMY_LINK_NAME;

	@Override
	public String getName() {
		return "aliasLink";
	}

	@Override
	public Link newLink(LinkConfig config) {
		return mock(Link.class);
	}

	@Override
	public LinkConfig newLinkConfig() {
		return NO_ATTRIBUTES;
	}

}