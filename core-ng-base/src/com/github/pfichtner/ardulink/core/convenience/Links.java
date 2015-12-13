package com.github.pfichtner.ardulink.core.convenience;

import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public final class Links {

	private static final Map<CacheKey, CacheValue> cache = new HashMap<CacheKey, CacheValue>();

	private Links() {
		super();
	}

	/**
	 * Returns the default Link which is a connection to the first serial port.
	 * 
	 * @return default Link
	 */
	public static Link getDefault() {
		try {
			return getLink(getDefaultConfigurer());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Configurer getDefaultConfigurer() {
		return setChoiceValues(getConfigurer());
	}

	private static Configurer getConfigurer() {
		URI serial = serialURI();
		LinkManager linkManager = linkManager();
		List<URI> availableURIs = linkManager.listURIs();
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

	private static URI serialURI() {
		try {
			return new URI("ardulink://serial");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a shared Link to the passed URI. If the Link alreay was created
	 * the cached Link is returned.
	 * 
	 * @param uri
	 *            the URI to create the Link for
	 * @return shared Link for the passed URI or a newly created one if no Link
	 *         for that URI exists
	 * @throws Exception
	 */
	public static Link getLink(URI uri) throws Exception {
		return getLink(linkManager().getConfigurer(uri));
	}

	public static Link getLink(Configurer configurer) throws Exception {
		final CacheKey cacheKey = new CacheKey(configurer);
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

	private static LinkDelegate newDelegate(final CacheKey cacheKey, Link link) {
		return new LinkDelegate(link) {
			@Override
			public void close() throws IOException {
				synchronized (cache) {
					if (checkNotNull(cache.get(cacheKey),
							"Link retrieved from cache but not found inside it")
							.decreaseUsageCounter() == 0) {
						cache.remove(cacheKey);
					}
				}
				super.close();
			}
		};
	}

	public static Configurer setChoiceValues(Configurer configurer) {
		for (String key : configurer.getAttributes()) {
			ConfigAttribute attribute = configurer.getAttribute(key);
			if (attribute.hasChoiceValues()) {
				Object[] choiceValues = attribute.getChoiceValues();
				// we use the first one for each
				if (choiceValues.length > 0) {
					attribute.setValue(choiceValues[0]);
				}
			}
		}
		return configurer;
	}

}
