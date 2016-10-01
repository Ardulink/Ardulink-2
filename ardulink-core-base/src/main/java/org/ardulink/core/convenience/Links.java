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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.URIs;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * This is a convenience layer for retrieving links. Links retrieved via this
 * class are cached and shared.
 * 
 * [adsense]
 */
public final class Links {

	// TODO use a WeakHashMap and use PhantomReferences to close GCed Links
	private static final Map<Object, CacheValue> cache = new HashMap<Object, CacheValue>();

	private Links() {
		super();
	}

	/**
	 * Returns the default Link which is a connection to the first serial port.
	 * 
	 * @return default Link
	 */
	public static Link getDefault() {
		return getLink(getDefaultConfigurer());
	}

	public static Configurer getDefaultConfigurer() {
		return setChoiceValues(getConfigurer());
	}

	private static Configurer getConfigurer() {
		LinkManager linkManager = linkManager();
		List<URI> availableURIs = linkManager.listURIs();
		URI serial = URIs.newURI("ardulink://serial");
		if (availableURIs.contains(serial)) {
			return linkManager.getConfigurer(serial);
		} else if (!availableURIs.isEmpty()) {
			return linkManager.getConfigurer(availableURIs.get(0));
		}
		throw new IllegalStateException("No factory registered");
	}

	private static LinkManager linkManager() {
		return LinkManager.getInstance();
	}

	/**
	 * Returns a shared Link to the passed URI. If the Link already was created
	 * the cached Link is returned. If the Link is not used anymore it should be
	 * closed by calling {@link Link#close()} on it. Doing so will decrease the
	 * usage counter and finally in cache eviction if it's unused.
	 * 
	 * @param uri
	 *            the URI to create the Link for
	 * @return shared Link for the passed URI or a newly created one if no Link
	 *         for that URI exists
	 * @throws Exception
	 */
	public static Link getLink(URI uri) {
		return isDefault(uri) ? getDefault() : getLink(linkManager()
				.getConfigurer(uri));
	}

	private static boolean isDefault(URI uri) {
		return "default".equalsIgnoreCase(extractNameFromURI(uri));
	}

	public static Link getLink(Configurer configurer) {
		final Object cacheKey = configurer.uniqueIdentifier();
		synchronized (cache) {
			CacheValue cacheValue = cache.get(cacheKey);
			if (cacheValue == null) {
				cache.put(
						cacheKey,
						(cacheValue = new CacheValue(newDelegate(cacheKey,
								configurer.newLink()))));
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
					if (cacheValue != null
							&& cacheValue.decreaseUsageCounter() == 0) {
						cache.remove(cacheKey);
						super.close();
					}
				}
			}
		};
	}

	public static Configurer setChoiceValues(Configurer configurer) {
		for (String key : configurer.getAttributes()) {
			ConfigAttribute attribute = configurer.getAttribute(key);
			if (attribute.hasChoiceValues() && !isConfigured(attribute)) {
				Object[] choiceValues = attribute.getChoiceValues();
				// we use the first one for each
				if (choiceValues.length > 0) {
					attribute.setValue(choiceValues[0]);
				}
			}
		}
		return configurer;
	}

	public static boolean isConfigured(ConfigAttribute attribute) {
		return attribute.getValue() != null;
	}

}
