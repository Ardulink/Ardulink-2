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

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static org.ardulink.util.Predicates.attribute;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class PredicatesTest {

	@Test
	void testNot() {
		assertThat(stream().filter(not(isEqual("22")))).containsExactly("1", "333");
	}

	@Test
	void testAttribute() {
		assertThat(stream().filter(attribute(String::length, isEqual(3)))).containsExactly("333");
	}

	Stream<String> stream() {
		return Stream.of("1", "22", "333");
	}

}