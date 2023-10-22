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
import static org.ardulink.core.linkmanager.Classloaders.getResources;
import static org.ardulink.core.linkmanager.Classloaders.moduleClassloader;
import static org.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;
import static org.ardulink.util.Classes.constructor;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Predicates.not;
import static org.ardulink.util.Strings.nullOrEmpty;
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
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class FactoriesViaMetaInfArdulink implements LinkFactoriesProvider {

	static class LineProcessor {

		/**
		 * A generic LinkFactory that has no implementation but a {@link LinkConfig} and
		 * {@link Link} that has a constructor accepting the {@link LinkConfig}.<br>
		 * Creating {@link Link} without {@link LinkConfig}s is also supported, when
		 * passing "null" as <code>configClassName</code> and the {@link Link} has a
		 * public zero arg constructor.
		 */
		private static final class GenericLinkFactory implements LinkFactory<LinkConfig> {

			private final ClassLoader classloader;
			private final String name;
			private final Class<? extends LinkConfig> configClass;
			private final Class<? extends Link> linkClass;
			private final Constructor<? extends Link> constructor;

			private GenericLinkFactory(ClassLoader classloader, String name, String configClassName,
					String linkClassName) throws ClassNotFoundException {
				this.classloader = classloader;
				this.name = name;
				this.configClass = loadConfigClass(configClassName);
				this.linkClass = loadClass(linkClassName, Link.class);
				this.constructor = determineConstructor(this.configClass, this.linkClass);
			}

			private static Constructor<? extends Link> determineConstructor(Class<? extends LinkConfig> configClass,
					Class<? extends Link> linkClass) {
				return isNullConfig(configClass) //
						? constructor(linkClass).orElseThrow(() -> new IllegalStateException(
								format("%s has no public zero arg constructor", linkClass.getName()))) //
						: constructor(linkClass, configClass).orElseThrow(() -> new IllegalStateException(
								format("%s has no public constructor with argument of type %s", linkClass.getName(),
										configClass.getName())));
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public Link newLink(LinkConfig config) throws Exception {
				try {
					return constructor.getParameterCount() == 0 //
							? constructor.newInstance() //
							: constructor.newInstance(config);
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
				return nullOrEmpty(configClassName) || "null".equalsIgnoreCase(configClassName);
			}

			private static boolean isNullConfig(Class<?> configClass) {
				return LinkConfig.class.equals(configClass);
			}

			private <T> Class<? extends T> loadClass(String name, Class<T> targetType) throws ClassNotFoundException {
				Class<?> clazz = this.classloader.loadClass(name);
				checkState(targetType.isAssignableFrom(clazz), "%s not of type %s", clazz.getName(),
						targetType.getName());
				return clazz.asSubclass(targetType);
			}

			@Override
			public LinkConfig newLinkConfig() {
				try {
					return isNullConfig(configClass) ? NO_ATTRIBUTES : configClass.newInstance();
				} catch (InstantiationException e) {
					throw propagate(e);
				} catch (IllegalAccessException e) {
					throw propagate(e);
				}
			}

		}

		private final ClassLoader classloader;

		LineProcessor(ClassLoader classloader) {
			this.classloader = classloader;
		}

		LinkFactory<LinkConfig> processLine(String line) {
			String[] split = line.split("\\:");
			checkState(split.length == 3, "Could not split %s into name:configclass:linkclass", line);
			try {
				return createLinkFactory(split[0], split[1], split[2]);
			} catch (ClassNotFoundException e) {
				throw propagate(e);
			}
		}

		private LinkFactory<LinkConfig> createLinkFactory(String name, String configClassName, String linkClassName)
				throws ClassNotFoundException {
			return new GenericLinkFactory(classloader, name, configClassName, linkClassName);
		}

	}

	@Override
	public Collection<LinkFactory> loadLinkFactories() {
		ClassLoader classloader = moduleClassloader();
		LineProcessor lineParser = new LineProcessor(classloader);
		try {
			return getResources(classloader, "META-INF/services/ardulink/linkfactory").stream()
					.map(url -> loadLinkFactories(lineParser, url)).flatMap(Collection::stream).collect(toList());
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private List<LinkFactory<LinkConfig>> loadLinkFactories(LineProcessor lineProcessor, URL url) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
			return reader.lines().filter(not(String::isEmpty)).map(lineProcessor::processLine).collect(toList());
		} catch (IOException e) {
			throw propagate(e);
		}
	}

}
