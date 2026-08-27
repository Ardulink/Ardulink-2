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

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class StreamsTest {

	@Test
	void getLast() {
		assertThat(Streams.getLast(Stream.of(1, 2, 3))).hasValue(3);
		assertThat(Streams.getLast(Stream.empty())).isEmpty();
	}

	@Test
	void iterator() {
		assertThat(Streams.iterator(Stream.of(null, 42, -1, null))).toIterable().containsExactly(null, 42, -1, null);
		assertThat(Streams.iterator(Stream.empty())).toIterable().isEmpty();
	}

	@Test
	void iterable() {
		assertThat(Streams.iterable(Stream.of(null, 42, -1, null))).containsExactly(null, 42, -1, null);
		assertThat(Streams.iterable(Stream.empty())).isEmpty();
	}

	@Test
	void concat() {
		assertThat(Streams.concat(Stream.of("a"), Stream.of("b"), Stream.of("c"))).containsExactly("a", "b", "c");
		assertThat(Streams.concat()).isEmpty();
	}

	@ParameterizedTest
	@MethodSource("castIfInstanceParameters")
	void castIfInstance(Object object, Class<CharSequence> clazz, List<String> expected) {
		assertThat(Streams.castIfInstance(clazz, object)).containsExactlyElementsOf(expected);
	}

	static Stream<Arguments> castIfInstanceParameters() {
		return Stream.of( //
				Arguments.of("foo", CharSequence.class, List.of("foo")), //
				Arguments.of(42, CharSequence.class, List.of()), //
				Arguments.of(null, CharSequence.class, List.of()));
	}

}
