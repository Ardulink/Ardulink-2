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

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

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

		private final Joiner joiner;
		private final String kvSeparator;

		public MapJoiner(Joiner joiner, String kvSeparator) {
			this.joiner = joiner;
			this.kvSeparator = kvSeparator;
		}

		public String join(Map<?, ?> map) {
			return joiner.join(map.entrySet().stream().map(this::entryToString));
		}

		private String entryToString(Entry<?, ?> entry) {
			return entry.getKey() + kvSeparator + entry.getValue();
		}

	}

	private final String separator;

	private Joiner(String separator) {
		this.separator = separator;
	}

	public static Joiner on(String separator) {
		return new Joiner(separator);
	}

	public String join(Collection<? extends Object> values) {
		return join(values.stream());
	}

	public String join(Object[] values) {
		return join(Arrays.stream(values));
	}

	public String join(Stream<? extends Object> stream) {
		return stream.map(String::valueOf).collect(joining(separator));
	}

	public MapJoiner withKeyValueSeparator(String kvSeparator) {
		return new MapJoiner(this, kvSeparator);
	}

}
