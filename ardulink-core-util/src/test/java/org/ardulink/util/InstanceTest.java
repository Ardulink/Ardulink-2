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

import static org.ardulink.util.Instance.instance;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

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
class InstanceTest {

	@ParameterizedTest
	@MethodSource("castIfInstanceParameters")
	void apply(Object object, Class<CharSequence> clazz, String expected) {
		assertThat(instance(clazz).apply(object)).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource("castIfInstanceParameters")
	void stream(Object object, Class<CharSequence> clazz, String expected) {
		List<String> expectedList = expected == null ? List.of() : List.of(expected);
		assertThat(instance(clazz).stream(object)).containsExactlyElementsOf(expectedList);
	}

	static Stream<Arguments> castIfInstanceParameters() {
		return Stream.of( //
				Arguments.of("foo", CharSequence.class, "foo"), //
				Arguments.of(42, CharSequence.class, null), //
				Arguments.of(null, CharSequence.class, null));
	}

}