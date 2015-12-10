package com.github.pfichtner.ardulink.core.convenience;

import com.github.pfichtner.ardulink.core.Link;

class CacheValue {

	private final Link link;
	private int usageCounter;

	public CacheValue(Link link) {
		this.link = link;
	}

	public Link getLink() {
		return link;
	}

	public int increaseUsageCounter() {
		return ++usageCounter;
	}

	public int decreaseUsageCounter() {
		return --usageCounter;
	}

}
