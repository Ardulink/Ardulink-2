package org.zu.ardulink.util;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K, V> {

	private final Map<K, V> data = new HashMap<K, V>();

	private MapBuilder() {
		super();
	}

	public static <K, V> MapBuilder<K, V> newMapBuilder() {
		return new MapBuilder<K, V>();
	}

	public MapBuilder<K, V> put(K key, V value) {
		data.put(key, value);
		return this;
	}

	public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> other) {
		data.putAll(other);
		return this;
	}

	public Map<K, V> build() {
		return new HashMap<K, V>(this.data);
	}

}
