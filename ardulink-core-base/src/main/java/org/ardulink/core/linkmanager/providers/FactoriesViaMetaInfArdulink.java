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

import static org.ardulink.core.linkmanager.Classloaders.moduleClassloader;
import static org.ardulink.util.Classes.constructor;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Throwables.propagate;
import static org.ardulink.util.Throwables.propagateIfInstanceOf;
import static org.ardulink.util.anno.LapsedWith.JDK8;

import java.io.BufferedReader;
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
import org.ardulink.util.Lists;
import org.ardulink.util.Strings;
import org.ardulink.util.Throwables;
import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class FactoriesViaMetaInfArdulink implements LinkFactoriesProvider {

	private static final class GenericLinkFactory implements
			LinkFactory<LinkConfig> {

		private final ClassLoader classloader;
		private final String name;
		private final String linkClassName;
		private final Class<? extends LinkConfig> configClass;

		private GenericLinkFactory(ClassLoader classloader, String name,
				String configClassName, String linkClassName)
				throws ClassNotFoundException {
			this.classloader = classloader;
			this.name = name;
			this.configClass = loadConfigClass(configClassName);
			this.linkClassName = linkClassName;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Link newLink(LinkConfig config) throws Exception {
			Class<? extends Link> linkClass = loadClass(linkClassName,
					Link.class);
			Class<? extends LinkConfig> configClass = getConfigClass();
			Constructor<? extends Link> constructor = constructor(linkClass, configClass).getOrThrow(
					"%s has no public constructor with argument of type %s", linkClass.getName(),
					configClass.getName());
			try {
				return constructor.newInstance(config);
			} catch (InvocationTargetException e) {
				propagateIfInstanceOf(e.getTargetException(), Error.class);
				propagateIfInstanceOf(e.getTargetException(), Exception.class);
				throw propagate(e);
			}
		}

		private Class<? extends LinkConfig> loadConfigClass(
				String configClassName) throws ClassNotFoundException {
			return isNull(configClassName) ? null : loadClass(configClassName,
					LinkConfig.class);
		}

		private static boolean isNull(String configClassName) {
			return Strings.nullOrEmpty(configClassName)
					|| "null".equalsIgnoreCase(configClassName);
		}

		private Class<? extends LinkConfig> getConfigClass() {
			return this.configClass == null ? LinkConfig.class
					: this.configClass;
		}

		private <T> Class<? extends T> loadClass(String name,
				Class<T> targetType) throws ClassNotFoundException {
			Class<?> clazz = this.classloader.loadClass(name);
			checkState(targetType.isAssignableFrom(clazz), "%s not of type %s",
					clazz.getName(), targetType.getName());
			return clazz.asSubclass(targetType);
		}

		@Override
		public LinkConfig newLinkConfig() {
			try {
				return configClass == null ? LinkConfig.NO_ATTRIBUTES
						: (LinkConfig) configClass.newInstance();
			} catch (InstantiationException e) {
				throw Throwables.propagate(e);
			} catch (IllegalAccessException e) {
				throw Throwables.propagate(e);
			}
		}
	}

	@Override
	@LapsedWith(module = JDK8, value = "BufferedReader#lines")
	public Collection<LinkFactory> loadLinkFactories() {
		List<LinkFactory> factories = Lists.newArrayList();
		try {
			ClassLoader classloader = moduleClassloader();
			for (URL url : Classloaders.getResources(classloader, "META-INF/services/ardulink/linkfactory")) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					if (!line.isEmpty()) {
						factories.add(processLine(classloader, line));
					}
				}
				reader.close();
			}
			return factories;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	private LinkFactory<LinkConfig> processLine(ClassLoader classloader,
			String line) throws ClassNotFoundException {
		String[] split = line.split("\\:");
		checkState(split.length == 3,
				"Could not split %s into name:configclass:linkclass", line);
		return createLinkFactory(classloader, split[0], split[1], split[2]);
	}

	private LinkFactory<LinkConfig> createLinkFactory(ClassLoader classloader,
			String name, String configClassName, String linkClassName)
			throws ClassNotFoundException {
		return new GenericLinkFactory(classloader, name, configClassName,
				linkClassName);
	}

}
