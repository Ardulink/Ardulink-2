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

import static org.ardulink.util.Maps.toProperties;
import static org.ardulink.util.Maps.valuesToString;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class MapsTest {

	Map<Integer, String> map = Map.of(1, "a", 2, "b", 3, "c");

	@Test
	void testToProperties() {
		assertThat(toProperties(map)).containsExactlyInAnyOrderEntriesOf(map);
	}

	@Test
	void testEntry() {
		Entry<Integer, String> entry = Maps.entry(1, "a");
		Entry<Integer, String> otherEntry = Maps.entry(1, "a");
		assertThat(entry).hasSameHashCodeAs(otherEntry).isEqualTo(otherEntry);
	}

	@Test
	void testValuesToString() {
		Map<Integer, Object> in = new HashMap<>(Map.of(1, "A", 2, 2, 4, false));
		in.put(3, null);
		Map<Integer, String> expected = Map.of(1, "A", 2, "2", 3, "null", 4, "false");
		assertThat(valuesToString(in)).containsExactlyInAnyOrderEntriesOf(expected);
	}

	@Test
	void testMerge() {
		Map<Integer, String> map1 = Map.of(1, "m1-A", 2, "m1-B", 3, "m1-C");
		Map<Integer, String> map2 = Map.of(2, "m2-B", 4, "m2-D");
		Map<Integer, String> merged = Maps.merge(map1, map2);
		assertThat(merged).containsExactlyInAnyOrderEntriesOf(Map.of(1, "m1-A", 2, "m2-B", 3, "m1-C", 4, "m2-D"));
	}

}
