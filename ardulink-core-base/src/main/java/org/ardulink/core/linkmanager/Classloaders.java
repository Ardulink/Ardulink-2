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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.ardulink.core.classloader.ModuleClassLoader;
import org.ardulink.util.Optional;

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
		return new ModuleClassLoader(parent, systemProperty("ardulink.module.dir").or("."));
	}

	private static Optional<String> systemProperty(String propertyName) {
		return Optional.ofNullable(System.getProperty(propertyName));
	}

	public static Collection<URL> getResources(ClassLoader classloader, String name) throws IOException {
		return new HashSet<URL>(Collections.list(classloader.getResources(name)));
	}

}
