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
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.ardulink.util.BinaryOperators.right;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Maps {

	private Maps() {
		super();
	}

	public static Properties toProperties(Map<? extends Object, ? extends Object> map) {
		Properties properties = new Properties();
		properties.putAll(map);
		return properties;
	}

	public static <K, V> Entry<K, V> entry(K k, V v) {
		// do not replace by JDK9's Map#entry since it does not support null values
		return new AbstractMap.SimpleEntry<>(k, v);
	}

	public static <K> Map<K, String> valuesToString(Map<K, Object> map) {
		return map.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
	}

	@SafeVarargs
	public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
		return Arrays.stream(maps) //
				.map(Map::entrySet) //
				.flatMap(Collection::stream)
				.collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, right()));
	}

}
