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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 *         [adsense]
 */
public class SetMultiMap<K, V> {

	private final Map<K, Set<V>> data = new HashMap<K, Set<V>>();

	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	public boolean put(K key, V value) {
		Set<V> values = this.data.get(key);
		if (values == null) {
			this.data.put(key, values = new HashSet<V>());
		}
		return values.add(value);
	}

	public boolean remove(K key, V value) {
		Set<V> values = this.data.get(key);
		boolean removed = values != null && values.remove(value);
		if (removed && values.isEmpty()) {
			this.data.remove(key);
		}
		return removed;
	}

	public void clear() {
		this.data.clear();
	}

	public Map<K, Set<V>> asMap() {
		return new HashMap<K, Set<V>>(data);
	}

}
