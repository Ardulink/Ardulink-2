package org.ardulink.core.linkmanager;

import static org.ardulink.core.linkmanager.Classloaders.moduleClassloader;

import java.util.List;
import java.util.ServiceLoader;

import org.ardulink.util.Lists;

public class FactoriesViaServiceLoader {

	public List<LinkFactory> loadLinkFactories() {
		return Lists.newArrayList(ServiceLoader.load(LinkFactory.class,
				moduleClassloader()).iterator());
	}

}
