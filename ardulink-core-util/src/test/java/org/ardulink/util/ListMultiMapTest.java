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

import java.util.Iterator;
import java.util.List;
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
class ListMultiMapTest {

	ListMultiMap<Integer, String> sut = new ListMultiMap<>();

	private static final int keyOne = 1;
	private static final String element1 = "one";
	private static final String element2 = "two";

	@Test
	void iteratorOnEmpty() {
		Iterator<Entry<Integer, String>> iterator = sut.iterator();
		assertThat(iterator).isExhausted();
	}

	@Test
	void iteratorOnSingleElement() {
		assertThat(sut.put(keyOne, element1)).isTrue();
		Iterator<Entry<Integer, String>> iterator = sut.iterator();
		Entry<Integer, String> next = iterator.next();
		assertThat(next.getKey()).isEqualTo(keyOne);
		assertThat(next.getValue()).isEqualTo(element1);
		assertThat(iterator).isExhausted();
	}

	@Test
	void iteratorOnCollisionElement() {
		assertThat(sut.put(keyOne, element1)).isTrue();
		assertThat(sut.put(keyOne, element2)).isTrue();
		Iterator<Entry<Integer, String>> iterator = sut.iterator();
		Entry<Integer, String> next = iterator.next();
		assertThat(next.getKey()).isEqualTo(keyOne);
		assertThat(next.getValue()).isEqualTo(element1);
		next = iterator.next();
		assertThat(next.getKey()).isEqualTo(keyOne);
		assertThat(next.getValue()).isEqualTo(element2);
		assertThat(iterator).isExhausted();
	}

	@Test
	void asMap() {
		assertThat(sut.put(keyOne, element1)).isTrue();
		assertThat(sut.put(keyOne, element2)).isTrue();
		assertThat(sut.put(2, "three")).isTrue();
		Map<Integer, List<String>> expected = Map.of(keyOne, List.of(element1, element2), 2, List.of("three"));
		assertThat(sut.asMap()).containsExactlyInAnyOrderEntriesOf(expected);
	}

}
