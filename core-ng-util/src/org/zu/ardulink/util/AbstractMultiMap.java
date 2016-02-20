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
package org.zu.ardulink.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 *         [adsense]
 */
public abstract class AbstractMultiMap<K, V> implements
		Iterable<Map.Entry<K, V>> {

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

	protected final Map<K, Collection<V>> data = new HashMap<K, Collection<V>>();

	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	public boolean put(K key, V value) {
		Collection<V> values = this.data.get(key);
		if (values == null) {
			this.data.put(key, values = make());
		}
		return values.add(value);
	}

	public boolean remove(K key, V value) {
		Collection<V> values = this.data.get(key);
		boolean removed = values != null && values.remove(value);
		if (removed && values.isEmpty()) {
			this.data.remove(key);
		}
		return removed;
	}

	protected abstract Collection<V> make();

	public void clear() {
		this.data.clear();
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return new Iterator<Map.Entry<K, V>>() {

			private final Iterator<Entry<K, Collection<V>>> it = data
					.entrySet().iterator();
			private K key;
			private Iterator<V> cur;

			@Override
			public boolean hasNext() {
				fetch();
				return cur != null && cur.hasNext();
			}

			private void fetch() {
				while ((cur == null || !cur.hasNext()) && it.hasNext()) {
					Entry<K, Collection<V>> next = it.next();
					key = next.getKey();
					cur = next.getValue().iterator();
				}
			}

			@Override
			public Entry<K, V> next() {
				fetch();
				return new MapEntry<K, V>(key, cur.next());
			}

			@Override
			public void remove() {
				fetch();
				cur.remove();
			}
		};
	}

}
