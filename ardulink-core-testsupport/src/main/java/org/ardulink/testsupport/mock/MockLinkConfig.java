package org.ardulink.testsupport.mock;

import org.ardulink.core.linkmanager.LinkConfig;

public class MockLinkConfig implements LinkConfig {

	@Named("name")
	public String name = "default";

	@Named("useThreadLocal")
	public boolean useThreadLocal;

}
