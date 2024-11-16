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
import java.util.Iterator;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class IteratorsTest {

	@Test
	void getFirst() {
		assertThat(Iterators.getFirst(iteratorOf(1))).hasValue(1);
		assertThat(Iterators.getFirst(iteratorOf(1, 2, null))).hasValue(1);
		assertThat(Iterators.getFirst(iteratorOf(null, 1, 2))).isEmpty();
	}

	@Test
	void getLast() {
		assertThat(Iterators.getLast(iteratorOf(1))).hasValue(1);
		assertThat(Iterators.getLast(iteratorOf(1, 2))).hasValue(2);
	}

	@Test
	void stream() {
		assertThat(Iterators.stream(iteratorOf(1))).containsExactly(1);
		assertThat(Iterators.stream(iteratorOf(1, 2))).containsExactly(1, 2);
		assertThat(Iterators.stream(iteratorOf(1, 2, null))).containsExactly(1, 2, null);
		assertThat(Iterators.stream(iteratorOf(null, 1, 2))).containsExactly(null, 1, 2);
		assertThat(Iterators.stream(iteratorOf())).isEmpty();
	}

	@SafeVarargs
	private final <T> Iterator<T> iteratorOf(T... elements) {
		return Arrays.asList(elements).iterator();
	}

}
