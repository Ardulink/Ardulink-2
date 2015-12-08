package com.github.pfichtner.ardulink.core.convenience;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public class Links {

	private static final ConcurrentMap<CacheKey, Link> cachedLinks = new ConcurrentHashMap<CacheKey, Link>();

	private static Link defaultLink = createDefaultLink();

	public static Link getDefault() {
		return defaultLink;
	}

	private static Link createDefaultLink() {
		try {
			return getConfigurer().newLink();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Configurer getConfigurer() {
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

	public static LinkManager linkManager() {
		return LinkManager.getInstance();
	}

	public static URI serialURI() {
		try {
			return new URI("ardulink://serial");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static Link getLink(URI uri) throws Exception {
		return getLink(linkManager().getConfigurer(uri));
	}

	public static Link getLink(Configurer configurer) throws Exception {
		CacheKey cacheKey = new CacheKey(configurer);
		Link link = cachedLinks.get(cacheKey);
		if (link == null) {
			link = configurer.newLink();
			Link tmp = cachedLinks.putIfAbsent(cacheKey, link);
			if (tmp != null) {
				return tmp;
			}
		}
		return link;
	}

}
