package org.ardulink.core.linkmanager;

import static org.ardulink.core.linkmanager.Classloaders.moduleClassloader;
import static org.ardulink.util.Iterables.forEnumeration;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Throwables.propagate;
import static org.ardulink.util.Throwables.propagateIfInstanceOf;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import org.ardulink.core.Link;
import org.ardulink.util.Lists;
import org.ardulink.util.Strings;
import org.ardulink.util.Throwables;

public class FactoriesViaMetaInfArdulink {

	private static final class GenericLinkFactory implements
			LinkFactory<LinkConfig> {

		private final ClassLoader classloader;
		private final String name;
		private final String linkClassName;
		private final Class<?> configClass;

		private GenericLinkFactory(ClassLoader classloader, String name,
				String configClassName, String linkClassName)
				throws ClassNotFoundException {
			this.classloader = classloader;
			this.name = name;
			this.configClass = loadConfigClass(configClassName);
			this.linkClassName = linkClassName;
		}

		private Class<?> loadConfigClass(String configClassName)
				throws ClassNotFoundException {
			if (Strings.nullOrEmpty(configClassName)
					|| "null".equalsIgnoreCase(configClassName)) {
				return null;
			}
			Class<?> loaded = loadClass(configClassName);
			checkArgument(LinkConfig.class.isAssignableFrom(loaded),
					"%s not subtype of %s", loaded.getName(),
					LinkConfig.class.getName());
			return loaded;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Link newLink(LinkConfig config) throws Exception {
			Class<? extends Link> linkClass = loadLinkClass(linkClassName,
					Link.class);
			Class<? extends Object> cClass = configClass == null ? LinkConfig.class
					: configClass;
			Constructor<?> constructor = checkNotNull(
					linkClass.getConstructor(cClass),
					"%s has no public constructor with argument of type %s",
					linkClass.getName(), cClass.getName());
			try {
				return linkClass.cast(constructor.newInstance(config));
			} catch (InvocationTargetException e) {
				propagateIfInstanceOf(e.getTargetException(), Error.class);
				propagateIfInstanceOf(e.getTargetException(), Exception.class);
				throw propagate(e);
			}
		}

		private <T> Class<? extends T> loadLinkClass(String name,
				Class<T> targetType) throws ClassNotFoundException {
			Class<?> clazz = loadClass(name);
			checkState(targetType.isAssignableFrom(clazz), "%s not of type %s",
					clazz.getName(), targetType.getName());
			return clazz.asSubclass(targetType);
		}

		private Class<?> loadClass(String name) throws ClassNotFoundException {
			return this.classloader.loadClass(name);
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

	public List<LinkFactory> loadLinkFactories() {
		List<LinkFactory> factories = Lists.newArrayList();
		try {
			ClassLoader classloader = moduleClassloader();
			for (URL url : forEnumeration(classloader
					.getResources("META-INF/services/ardulink/linkfactory"))) {
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
