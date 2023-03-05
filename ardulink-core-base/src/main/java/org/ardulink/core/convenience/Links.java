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

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static org.ardulink.core.linkmanager.LinkManager.extractNameFromURI;
import static org.ardulink.core.linkmanager.LinkManager.replaceName;
import static org.ardulink.util.Iterables.getFirst;
import static org.ardulink.util.Lists.sortedCopy;
import static org.ardulink.util.Streams.getFirst;

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
import org.ardulink.util.URIs;

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
	private static final Map<Object, CacheValue> cache = new HashMap<>();
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
			return !isAliasName(name) && aliasFor.matcher(name).matches();
		}

	}

	private static final Alias serialAlias = new Alias("serial", Pattern.compile("serial\\-.+"));
	private static final List<Alias> aliases = Arrays.asList(new Alias("default", Pattern.compile(".*")), serialAlias);

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
		return setChoiceValues(linkManager.getConfigurer(getFirst(sorted(linkManager.listURIs()))
				.orElseThrow(() -> new IllegalStateException("No factory registered"))));
	}

	private static List<URI> sorted(List<URI> uris) {
		return sortedCopy(uris, serialsFirst());
	}

	private static Comparator<URI> serialsFirst() {
		return comparing(uri -> {
			String name = extractNameFromURI(uri);
			return serialAlias.isAliasName(name) ? -2 : serialAlias.isAliasFor(name) ? -1 : 0;
		});
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
		return getLink(linkManager.getConfigurer(aliasReplacement(uri)));
	}

	private static URI aliasReplacement(URI uri) {
		List<URI> availableUris = sorted(linkManager.listURIs());
		String name = extractNameFromURI(uri);
		return containsName(availableUris, name) ? uri
				: findAlias(name).map(a -> isAliasFor(availableUris, a).orElse(null))
						.map(r -> replaceName(uri, extractNameFromURI(r))).orElse(uri);
	}

	private static java.util.Optional<URI> isAliasFor(List<URI> availableUris, Alias alias) {
		return availableUris.stream().filter(u -> alias.isAliasFor(extractNameFromURI(u))).findFirst();
	}

	private static java.util.Optional<Alias> findAlias(String name) {
		return getFirst(aliases.stream().filter(alias -> alias.isAliasName(name)));
	}

	private static boolean containsName(List<URI> uris, String name) {
		return uris.stream().anyMatch(uri -> extractNameFromURI(uri).equals(name));
	}

	public static Link getLink(Configurer configurer) {
		Object cacheKey = configurer.uniqueIdentifier();
		synchronized (cache) {
			CacheValue cacheValue = cache.get(cacheKey);
			if (cacheValue == null) {
				cache.put(cacheKey, (cacheValue = new CacheValue(newDelegate(cacheKey, configurer.newLink()))));
			}
			cacheValue.increaseUsageCounter();
			return cacheValue.getLink();
		}
	}

	private static LinkDelegate newDelegate(Object cacheKey, Link link) {
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

			private final ConcurrentHashMap<Pin, AtomicInteger> listenCounter = new ConcurrentHashMap<>();

			@Override
			public long startListening(Pin pin) throws IOException {
				long result = super.startListening(pin);
				listenCounter.merge(pin, new AtomicInteger(1), (i1, i2) -> new AtomicInteger(i1.addAndGet(i2.get())));
				return result;
			}

			@Override
			public long stopListening(Pin pin) throws IOException {
				AtomicInteger counter = listenCounter.get(pin);
				return counter != null && counter.decrementAndGet() == 0 ? super.stopListening(pin) : -1;
			}
		};
	}

	public static Configurer setChoiceValues(Configurer configurer) {
		configurer.getAttributes().stream().map(k -> configurer.getAttribute(k))
				.filter(a -> a.hasChoiceValues() && !isConfigured(a))
				.forEach(a -> stream(a.getChoiceValues()).findFirst().ifPresent(a::setValue));
		return configurer;
	}

	public static boolean isConfigured(ConfigAttribute attribute) {
		return attribute.getValue() != null;
	}

}
