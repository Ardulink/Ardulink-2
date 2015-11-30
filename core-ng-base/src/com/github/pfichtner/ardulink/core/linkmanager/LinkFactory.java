package com.github.pfichtner.ardulink.core.linkmanager;

import com.github.pfichtner.ardulink.core.Link;

public interface LinkFactory<T extends LinkConfig> {

	String getName();

	Link newLink(T config) throws Exception;

	T newLinkConfig();

}
