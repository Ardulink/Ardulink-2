package com.github.pfichtner.ardulink.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMultiMap<K, V> {

	private final Map<K, List<V>> data = new HashMap<K, List<V>>();

	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	public void put(K key, V value) {
		List<V> values = this.data.get(key);
		if (values == null) {
			this.data.put(key, values = new ArrayList<V>());
		}
		values.add(value);
	}

	public void clear() {
		this.data.clear();
	}

	public Map<K, List<V>> asMap() {
		return new HashMap<K, List<V>>(data);
	}

}
