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

import static java.util.Collections.singleton;
import static org.ardulink.util.Maps.entry;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class SetMultiMapTest {

	SetMultiMap<Integer, String> sut = new SetMultiMap<>();

	private static final int key = 1;
	private static final String element1 = "foo";
	private static final String element2 = "bar";

	@Test
	void canPut() {
		assertThat(sut.put(key, element1)).isTrue();
		assertThat(sut.asMap()).containsExactly(entry(key, singleton(element1)));
	}

	@Test
	void canPutTwice() {
		assertThat(sut.put(key, element1)).isTrue();
		assertThat(sut.put(key, element1)).isFalse();
		assertThat(sut.asMap()).containsExactly(entry(key, singleton(element1)));
	}

	@Test
	void canRemoveExistingValue() {
		assertThat(sut.put(key, element1)).isTrue();
		assertThat(sut.remove(key, element1)).isTrue();
		assertThat(sut.asMap()).isEmpty();
	}

	@Test
	void canHandleRemovesOfNonExistingValues() {
		assertThat(sut.put(key, element1)).isTrue();
		assertThat(sut.remove(key, element2)).isFalse();
		assertThat(sut.asMap()).containsExactly(entry(key, singleton(element1)));
	}

	@Test
	void canHandleRemovesOfNonExistingKeys() {
		assertThat(sut.put(key, element1)).isTrue();
		assertThat(sut.remove(not(key), element1)).isFalse();
		assertThat(sut.asMap()).containsExactly(entry(key, singleton(element1)));
	}

	private int not(int value) {
		return value + 1;
	}

}
