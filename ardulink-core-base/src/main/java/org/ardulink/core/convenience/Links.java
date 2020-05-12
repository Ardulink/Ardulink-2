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

package org.ardulink.core.convenience;

import static org.ardulink.core.linkmanager.LinkManager.extractNameFromURI;
import static org.ardulink.core.linkmanager.LinkManager.replaceName;
import static org.ardulink.util.Iterables.getFirst;
import static org.ardulink.util.Lists.sortedCopy;
import static org.ardulink.util.anno.LapsedWith.JDK8;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.Integers;
import org.ardulink.util.Optional;
import org.ardulink.util.URIs;
import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * This is a convenience layer for retrieving links. Links retrieved via this
 * class are cached and can be shared.
 * 
 * [adsense]
 */
public final class Links {

	// TODO use a WeakHashMap and use PhantomReferences to close GCed Links
	private static final Map<Object, CacheValue> cache = new HashMap<Object, CacheValue>();
	private static final LinkManager linkManager = LinkManager.getInstance();

	private Links() {
		super();
	}

	private static class Alias {
		private final String aliasName;
		private final Pattern aliasFor;

		public Alias(String aliasName, Pattern aliasFor) {
			this.aliasName = aliasName;
			this.aliasFor = aliasFor;
		}

		private boolean isAliasName(String name) {
			return name.equals(aliasName);
		}

		public boolean isAliasFor(String name) {
			return aliasFor.matcher(name).matches();
		}

	}

	private static final Alias serialAlias = new Alias("serial", Pattern.compile("serial\\-.+"));
	private static final List<Alias> aliases = Arrays.asList(serialAlias);

	/**
	 * Returns the default Link which is a connection to the first serial port if
	 * the serial module is available. Otherwise the first available link is
	 * returned. If no links are available a {@link RuntimeException} will be
	 * thrown.
	 * 
	 * @return default Link
	 */
	public static Link getDefault() {
		return getLink(getDefaultConfigurer());
	}

	public static Configurer getDefaultConfigurer() {
		return setChoiceValues(linkManager.getConfigurer(defaultUri()));
	}

	private static URI defaultUri() {
		return getFirst(sortedCopy(linkManager.listURIs(), serialsFirst())).getOrThrow(IllegalStateException.class,
				"No factory registered");
	}

	@LapsedWith(module = JDK8, value = "Comparator")
	private static Comparator<URI> serialsFirst() {
		return new Comparator<URI>() {
			@Override
			public int compare(URI uri1, URI uri2) {
				return Integers.compare(valueOf(uri1), valueOf(uri2));
			}

			private int valueOf(URI uri) {
				String name = extractNameFromURI(uri);
				return serialAlias.isAliasName(name) ? -2 : serialAlias.isAliasFor(name) ? -1 : 0;
			}

		};
	}

	/**
	 * Returns a shared Link to the passed URI. If the Link already was created the
	 * cached Link is returned. If the Link is not used anymore it should be closed
	 * by calling {@link Link#close()} on it. Doing so will decrease the usage
	 * counter and finally in cache eviction if it's unused.
	 * 
	 * @param uri the URI to create the Link for
	 * @return shared Link for the passed URI or a newly created one if no Link for
	 *         that URI exists
	 * @throws Exception
	 */
	public static Link getLink(String uri) {
		return getLink(URIs.newURI(uri));
	}

	/**
	 * Returns a shared Link to the passed URI. If the Link already was created the
	 * cached Link is returned. If the Link is not used anymore it should be closed
	 * by calling {@link Link#close()} on it. Doing so will decrease the usage
	 * counter and finally in cache eviction if it's unused.
	 * 
	 * @param uri the URI to create the Link for
	 * @return shared Link for the passed URI or a newly created one if no Link for
	 *         that URI exists
	 */
	public static Link getLink(URI uri) {
		return isDefault(uri) ? getDefault() : getLink(linkManager.getConfigurer(aliasReplacement(uri)));
	}

	@LapsedWith(module = JDK8, value = "Optional#map")
	private static URI aliasReplacement(URI uri) {
		List<URI> availableUris = linkManager.listURIs();
		String name = extractNameFromURI(uri);
		if (!containsName(availableUris, name)) {
			Optional<Alias> alias = findAlias(name);
			if (alias.isPresent()) {
				Optional<URI> replacement = aliasReplacement(availableUris, alias.get());
				if (replacement.isPresent()) {
					return replaceName(uri, extractNameFromURI(replacement.get()));
				}
			}
		}
		return uri;
	}

	@LapsedWith(module = JDK8, value = "Stream")
	private static Optional<URI> aliasReplacement(List<URI> availableUris, Alias alias) {
		for (URI uri : availableUris) {
			if (alias.isAliasFor(extractNameFromURI(uri))) {
				return Optional.of(uri);
			}
		}
		return Optional.absent();
	}

	@LapsedWith(module = JDK8, value = "Stream")
	private static Optional<Alias> findAlias(String name) {
		for (Alias alias : aliases) {
			if (alias.isAliasName(name)) {
				return Optional.of(alias);
			}
		}
		return Optional.<Alias>absent();
	}

	@LapsedWith(module = JDK8, value = "Stream")
	private static boolean containsName(List<URI> uris, String name) {
		for (URI uri : uris) {
			if (extractNameFromURI(uri).equals(name)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isDefault(URI uri) {
		return "default".equalsIgnoreCase(extractNameFromURI(uri));
	}

	public static Link getLink(Configurer configurer) {
		final Object cacheKey = configurer.uniqueIdentifier();
		synchronized (cache) {
			CacheValue cacheValue = cache.get(cacheKey);
			if (cacheValue == null) {
				cache.put(cacheKey, (cacheValue = new CacheValue(newDelegate(cacheKey, configurer.newLink()))));
			}
			cacheValue.increaseUsageCounter();
			return cacheValue.getLink();
		}
	}

	private static LinkDelegate newDelegate(final Object cacheKey, Link link) {
		return new LinkDelegate(link) {
			@Override
			public void close() throws IOException {
				synchronized (cache) {
					CacheValue cacheValue = cache.get(cacheKey);
					if (cacheValue != null && cacheValue.decreaseUsageCounter() == 0) {
						cache.remove(cacheKey);
						super.close();
					}
				}
			}

			private final ConcurrentHashMap<Pin, AtomicInteger> listenCounter = new ConcurrentHashMap<Pin, AtomicInteger>();

			@Override
			public long startListening(Pin pin) throws IOException {
				long result = super.startListening(pin);
				AtomicInteger counter = getCounter(pin);
				if (counter == null) {
					@LapsedWith(module = JDK8, value = "Map#merge")
					AtomicInteger oldValue = listenCounter.putIfAbsent(pin, counter = new AtomicInteger());
					if (oldValue != null) {
						counter = oldValue;
					}
				}
				counter.getAndIncrement();
				return result;
			}

			@Override
			public long stopListening(Pin pin) throws IOException {
				AtomicInteger counter = getCounter(pin);
				return counter != null && counter.decrementAndGet() == 0 ? super.stopListening(pin) : -1;
			}

			private AtomicInteger getCounter(Pin pin) {
				return listenCounter.get(pin);
			}
		};
	}

	@LapsedWith(module = JDK8, value = "Stream")
	public static Configurer setChoiceValues(Configurer configurer) {
		for (String key : configurer.getAttributes()) {
			ConfigAttribute attribute = configurer.getAttribute(key);
			if (attribute.hasChoiceValues() && !isConfigured(attribute)) {
				@LapsedWith(module = JDK8, value = "Optional")
				Optional<Object> first = getFirst(Arrays.asList(attribute.getChoiceValues()));
				if (first.isPresent()) {
					attribute.setValue(first.get());
				}
			}
		}
		return configurer;
	}

	public static boolean isConfigured(ConfigAttribute attribute) {
		return attribute.getValue() != null;
	}

}
