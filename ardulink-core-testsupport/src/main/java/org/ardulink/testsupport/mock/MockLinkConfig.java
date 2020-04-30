package org.ardulink.testsupport.mock;

import org.ardulink.core.linkmanager.LinkConfig;

public class MockLinkConfig implements LinkConfig {

	@Named("name")
	private String name = "default";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
