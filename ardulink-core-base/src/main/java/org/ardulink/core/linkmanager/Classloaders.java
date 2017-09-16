package org.ardulink.core.linkmanager;

import org.ardulink.core.classloader.ModuleClassLoader;
import org.ardulink.util.Optional;

public final class Classloaders {

	private Classloaders() {
		super();
	}

	public static ClassLoader moduleClassloader() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		return new ModuleClassLoader(classLoader, systemProperty(
				"ardulink.module.dir").or("."));
	}

	private static Optional<String> systemProperty(String propertyName) {
		return Optional.ofNullable(System.getProperty(propertyName));
	}

}
