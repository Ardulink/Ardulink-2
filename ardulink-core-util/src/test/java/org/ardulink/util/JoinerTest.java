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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ardulink.util.Joiner.MapJoiner;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class JoinerTest {

	@Test
	void join() {
		List<Object> objects = Arrays.asList("A", null, 1);
		Joiner sut = Joiner.on("; ");
		String expected = "A; null; 1";
		assertThat(sut.join(objects.toArray())).isEqualTo(expected);
		assertThat(sut.join(objects)).isEqualTo(expected);
	}

	@Test
	void joinMap() {
		Map<Object, Object> map = mapWithNullValue();
		MapJoiner sut = Joiner.on(", ").withKeyValueSeparator(" = ");
		String expected = "A = 42, " //
				+ "2 = null, " //
				+ "3 = 3";
		assertThat(sut.join(map)).isEqualTo(expected);
	}

	private static Map<Object, Object> mapWithNullValue() {
		Map<Object, Object> map = new HashMap<>();
		map.put("A", 42);
		map.put(2, null);
		map.put(3, "3");
		return map;
	}

}
