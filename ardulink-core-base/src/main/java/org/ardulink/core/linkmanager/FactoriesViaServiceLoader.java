package org.ardulink.core.linkmanager;

import java.util.List;
import java.util.ServiceLoader;

import org.ardulink.core.classloader.ModuleClassLoader;
import org.ardulink.util.Lists;
import org.ardulink.util.Optional;

public class FactoriesViaServiceLoader {

	public List<LinkFactory> loadLinkFactories() {
		return Lists.newArrayList(ServiceLoader.load(LinkFactory.class,
				classloader()).iterator());
	}

	private ClassLoader classloader() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		return new ModuleClassLoader(classLoader, systemProperty(
				"ardulink.module.dir").or("."));
	}

	private Optional<String> systemProperty(String propertyName) {
		return Optional.ofNullable(System.getProperty(propertyName));
	}

}
