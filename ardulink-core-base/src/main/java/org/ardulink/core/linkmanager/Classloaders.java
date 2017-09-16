package org.ardulink.core.linkmanager;

import org.ardulink.core.classloader.ModuleClassLoader;
import org.ardulink.util.Optional;

public final class Classloaders {

	private Classloaders() {
		super();
	}

	public static ClassLoader moduleClassloader() {
		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		return new ModuleClassLoader(parent, systemProperty(
				"ardulink.module.dir").or("."));
	}

	private static Optional<String> systemProperty(String propertyName) {
		return Optional.ofNullable(System.getProperty(propertyName));
	}

}
