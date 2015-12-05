package org.zu.ardulink.connection.proxy;

import com.github.pfichtner.ardulink.core.Link;

public class LinkContainer {

	private final Link link;
	private final CacheKey cacheKey;
	private int usageCounter = 1;

	public LinkContainer(Link link, CacheKey cacheKey) {
		this.link = link;
		this.cacheKey = cacheKey;
	}

	public Link getLink() {
		return link;
	}

	public CacheKey getCacheKey() {
		return cacheKey;
	}

	public int increaseUsageCounter() {
		return ++usageCounter;
	}

	public int decreaseUsageCounter() {
		return --usageCounter;
	}

}