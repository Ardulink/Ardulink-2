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

import static java.util.Arrays.asList;
import static org.ardulink.util.Lists.mapList;
import static org.ardulink.util.Maps.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ListsTest {

	@Test
	void testNewArrayListIterableOfT() {
		verifyIsMutable(Lists.newArrayList(asList("a", "b", "c")));
	}

	@Test
	void testNewArrayListIteratorOfT() {
		verifyIsMutable(Lists.newArrayList(asList("a", "b", "c").iterator()));
	}

	@Test
	void testNewArrayListTArray() {
		verifyIsMutable(Lists.newArrayList("a", "b", "c"));
	}

	@Test
	void testMapList() {
		assertThat(mapList(asList(entry(1, "A"), entry(3, "C"), entry(2, "B")), Map.Entry::getKey)) //
				.containsExactly(1, 3, 2);
	}

	private static void verifyIsMutable(List<String> list) {
		list.add("d");
		list.remove("d");
		list.clear();
	}

}
