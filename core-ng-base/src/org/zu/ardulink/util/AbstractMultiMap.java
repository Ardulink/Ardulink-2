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
import java.util.Map;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public abstract class AbstractMultiMap<K, V> {

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

}
