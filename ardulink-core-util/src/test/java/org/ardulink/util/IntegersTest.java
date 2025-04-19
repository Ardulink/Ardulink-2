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

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class IntegersTest {

	private static final List<Integer> oneToFive = List.of(1, 2, 3, 4, 5);

	@Test
	void sum() {
		assertThat(Integers.sum(oneToFive)).isEqualTo(15);
	}

	@Test
	void average() {
		assertThat(Integers.average(oneToFive)).isEqualTo(15 / 5);
	}

	@Test
	void constrain() {
		assertThat(Integers.constrain(42, 0, 127)).isEqualTo(42);
		assertThat(Integers.constrain(-1, 0, 127)).isEqualTo(0);
		assertThat(Integers.constrain(128, 0, 127)).isEqualTo(127);
	}

}
