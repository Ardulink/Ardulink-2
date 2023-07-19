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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
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
public final class Maps {

	private Maps() {
		super();
	}

	public static Properties toProperties(Map<? extends Object, ? extends Object> map) {
		Properties properties = new Properties();
		properties.putAll(map);
		return properties;
	}

	@LapsedWith(module = JDK9, value = "Map#entry")
	public static <K, V> Entry<K, V> entry(K k, V v) {
		return new AbstractMap.SimpleEntry<>(k, v);
	}

}
