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

import static org.ardulink.util.anno.LapsedWith.JDK8;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ardulink.util.anno.LapsedWith;

import java.util.Set;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Joiner {

	public static class MapJoiner {

		private final String separator;
		private final String kvSeparator;

		public MapJoiner(String separator, String kvSeparator) {
			this.separator = separator;
			this.kvSeparator = kvSeparator;
		}

		public String join(Map<?, ?> map) {
			Set<?> entrySet = map.entrySet();
			@SuppressWarnings("unchecked")
			Iterator<Entry<Object, Object>> it = (Iterator<Entry<Object, Object>>) entrySet.iterator();
			if (!it.hasNext()) {
				return "";
			}
			StringBuilder sb = append(it, new StringBuilder());
			while (it.hasNext()) {
				sb = append(it, sb.append(separator));
			}
			return sb.toString();
		}

		private StringBuilder append(Iterator<Entry<Object, Object>> iterator, StringBuilder sb) {
			Entry<Object, Object> entry = iterator.next();
			return sb.append(entry.getKey()).append(kvSeparator).append(entry.getValue());
		}

	}

	private final String separator;

	private Joiner(String separator) {
		this.separator = separator;
	}

	public static Joiner on(String separator) {
		return new Joiner(separator);
	}

	@LapsedWith(value = JDK8, module = "Stream/Collectors#joining")
	public String join(Iterable<? extends Object> values) {
		Iterator<? extends Object> iterator = values.iterator();
		if (!iterator.hasNext()) {
			return "";
		}
		StringBuilder sb = new StringBuilder().append(iterator.next());
		while (iterator.hasNext()) {
			sb = sb.append(separator).append(iterator.next());
		}
		return sb.toString();
	}

	public MapJoiner withKeyValueSeparator(String kvSeparator) {
		return new MapJoiner(this.separator, kvSeparator);
	}

}
