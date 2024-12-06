/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.core.linkmanager;

import static java.lang.System.getProperty;
import static java.util.Collections.list;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.ardulink.core.classloader.ModuleClassLoader;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Classloaders {

	private Classloaders() {
		super();
	}

	public static ClassLoader moduleClassloader() {
		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		return new ModuleClassLoader(parent, systemProperty("ardulink.module.dir").orElse("."));
	}

	private static Optional<String> systemProperty(String propertyName) {
		return ofNullable(getProperty(propertyName));
	}

	public static Collection<URL> getResources(ClassLoader classloader, String name) throws IOException {
		return Set.copyOf(list(classloader.getResources(name)));
	}

}
