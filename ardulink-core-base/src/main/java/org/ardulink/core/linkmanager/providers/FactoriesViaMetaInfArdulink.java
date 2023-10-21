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

package org.ardulink.core.linkmanager.providers;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.ardulink.core.linkmanager.Classloaders.moduleClassloader;
import static org.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;
import static org.ardulink.util.Classes.constructor;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Predicates.not;
import static org.ardulink.util.Throwables.propagate;
import static org.ardulink.util.Throwables.propagateIfInstanceOf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.Classloaders;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.util.Strings;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class FactoriesViaMetaInfArdulink implements LinkFactoriesProvider {

	static final class GenericLinkFactory implements LinkFactory<LinkConfig> {

		private final ClassLoader classloader;
		private final String name;
		private final Class<? extends LinkConfig> configClass;
		private final Class<? extends Link> linkClass;
		private final Constructor<? extends Link> constructor;

		GenericLinkFactory(ClassLoader classloader, String name, String configClassName, String linkClassName)
				throws ClassNotFoundException {
			this.classloader = classloader;
			this.name = name;
			this.configClass = loadConfigClass(configClassName);
			this.linkClass = loadClass(linkClassName, Link.class);
			this.constructor = constructor(linkClass, configClass).orElseThrow(
					() -> new IllegalStateException(format("%s has no public constructor with argument of type %s",
							linkClass.getName(), configClass.getName())));
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Link newLink(LinkConfig config) throws Exception {
			try {
				return constructor.newInstance(config);
			} catch (InvocationTargetException e) {
				propagateIfInstanceOf(e.getTargetException(), Error.class);
				propagateIfInstanceOf(e.getTargetException(), Exception.class);
				throw propagate(e);
			}
		}

		private Class<? extends LinkConfig> loadConfigClass(String configClassName) throws ClassNotFoundException {
			return isNull(configClassName) ? LinkConfig.class : loadClass(configClassName, LinkConfig.class);
		}

		private static boolean isNull(String configClassName) {
			return Strings.nullOrEmpty(configClassName) || "null".equalsIgnoreCase(configClassName);
		}

		private <T> Class<? extends T> loadClass(String name, Class<T> targetType) throws ClassNotFoundException {
			Class<?> clazz = this.classloader.loadClass(name);
			checkState(targetType.isAssignableFrom(clazz), "%s not of type %s", clazz.getName(), targetType.getName());
			return clazz.asSubclass(targetType);
		}

		@Override
		public LinkConfig newLinkConfig() {
			try {
				return LinkConfig.class.equals(configClass) ? NO_ATTRIBUTES : configClass.newInstance();
			} catch (InstantiationException e) {
				throw propagate(e);
			} catch (IllegalAccessException e) {
				throw propagate(e);
			}
		}
	}

	@Override
	public Collection<LinkFactory> loadLinkFactories() {
		try {
			ClassLoader classloader = moduleClassloader();
			return Classloaders.getResources(classloader, "META-INF/services/ardulink/linkfactory").stream()
					.map(url -> read(classloader, url)).flatMap(Collection::stream).collect(toList());
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private List<LinkFactory<LinkConfig>> read(ClassLoader classloader, URL url) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
			return reader.lines().filter(not(String::isEmpty)).map(l -> processLine(classloader, l)).collect(toList());
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private LinkFactory<LinkConfig> processLine(ClassLoader classloader, String line) {
		String[] split = line.split("\\:");
		checkState(split.length == 3, "Could not split %s into name:configclass:linkclass", line);
		try {
			return createLinkFactory(classloader, split[0], split[1], split[2]);
		} catch (ClassNotFoundException e) {
			throw propagate(e);
		}
	}

	private LinkFactory<LinkConfig> createLinkFactory(ClassLoader classloader, String name, String configClassName,
			String linkClassName) throws ClassNotFoundException {
		return new GenericLinkFactory(classloader, name, configClassName, linkClassName);
	}

}
