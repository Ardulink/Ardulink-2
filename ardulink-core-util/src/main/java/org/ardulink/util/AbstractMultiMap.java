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
package org.ardulink.util;

import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class AbstractMultiMap<K, V, T extends Collection<V>> implements Iterable<Entry<K, V>> {

	public static class MapEntry<K, V> implements Entry<K, V> {

		private final K key;
		private final V value;

		public MapEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}
	}

	protected final Map<K, T> data = new HashMap<>();

	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	public boolean put(K key, V value) {
		return this.data.computeIfAbsent(key, k -> make()).add(value);
	}

	public boolean remove(K key, V value) {
		T values = this.data.get(key);
		boolean removed = values != null && values.remove(value);
		if (removed && values.isEmpty()) {
			this.data.remove(key);
		}
		return removed;
	}

	protected abstract T make();

	private Set<Entry<K, T>> entrySet() {
		return data.entrySet();
	}

	public Map<K, T> asMap() {
		return entrySet().stream().collect(toMap(Entry::getKey, Entry::getValue));
	}

	public void clear() {
		this.data.clear();
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return new Iterator<Entry<K, V>>() {

			private final Iterator<Entry<K, T>> it = entrySet().iterator();
			private K key;
			private Iterator<V> cur;

			@Override
			public boolean hasNext() {
				fetch();
				return cur != null && cur.hasNext();
			}

			private void fetch() {
				while ((cur == null || !cur.hasNext()) && it.hasNext()) {
					Entry<K, T> next = it.next();
					key = next.getKey();
					cur = next.getValue().iterator();
				}
			}

			@Override
			public Entry<K, V> next() {
				fetch();
				return new MapEntry<>(key, cur.next());
			}

			@Override
			public void remove() {
				fetch();
				cur.remove();
			}
		};
	}

}
