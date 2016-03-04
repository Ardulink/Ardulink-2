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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 *  
 * 
 * [adsense]
 */
public class SetMultiMap<K, V> extends AbstractMultiMap<K, V> {

	@Override
	protected Collection<V> make() {
		return new HashSet<V>();
	}

	public Map<K, Set<V>> asMap() {
		Map<K, Set<V>> map = new HashMap<K, Set<V>>();
		for (Entry<K, Collection<V>> entry : data.entrySet()) {
			map.put(entry.getKey(), (Set<V>) entry.getValue());
		}
		return map;
	}

}
