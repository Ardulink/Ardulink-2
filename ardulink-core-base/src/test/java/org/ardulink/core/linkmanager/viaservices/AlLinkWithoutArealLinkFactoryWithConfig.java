package org.ardulink.core.linkmanager.viaservices;

import static org.mockito.Mockito.mock;

import org.ardulink.core.convenience.LinkDelegate;

public class AlLinkWithoutArealLinkFactoryWithConfig extends LinkDelegate {

	public AlLinkWithoutArealLinkFactoryWithConfig(AlLinkWithoutArealLinkFactoryConfig config) {
		super(mock(LinkDelegate.class));
	}

}
