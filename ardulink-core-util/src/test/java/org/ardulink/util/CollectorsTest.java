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

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
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
class CollectorsTest {

	List<Integer> elements = Arrays.asList(1, 2, 3);

	@Test
	void toUnmodifiableList() {
		List<Integer> list = elements.stream().collect(Collectors.toUnmodifiableList());
		assertThatThrownBy(() -> list.add(4)).isInstanceOf(RuntimeException.class);
	}

	@Test
	void toUnmodifiableMap() {
		Map<Integer, String> map = elements.stream().collect(Collectors.toUnmodifiableMap(identity(), String::valueOf));
		assertThatThrownBy(() -> map.put(4, "4")).isInstanceOf(RuntimeException.class);
	}

}
