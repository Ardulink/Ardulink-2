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

import static org.ardulink.util.anno.LapsedWith.JDK9;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@LapsedWith(value = JDK9, module = "Map#of")
public class MapBuilder<K, V> {

	private final Map<K, V> data = new HashMap<>();

	private MapBuilder() {
		super();
	}

	public static <K, V> MapBuilder<K, V> newMapBuilder() {
		return new MapBuilder<>();
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
		return new HashMap<>(this.data);
	}

	public Properties asProperties() {
		return Maps.toProperties(build());
	}

}
