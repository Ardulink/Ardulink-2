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
import java.util.Map;

import org.ardulink.util.Joiner.MapJoiner;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class JoinerTest {

	@Test
	void join() {
		Object[] objects = new Object[] { "A", null, 1 };
		Joiner joiner = Joiner.on("; ");
		String expected = "A; null; 1";
		assertThat(joiner.join(objects)).isEqualTo(expected);
		assertThat(joiner.join(Arrays.asList(objects))).isEqualTo(expected);
	}

	@Test
	void joinMap() {
		Map<Object, Object> map = MapBuilder.newMapBuilder().put("A", 42).put(2, null).put(3, "3").build();
		String separator = "|&>";
		String kvSeparator = " =>";
		MapJoiner withKeyValueSeparator = Joiner.on(separator).withKeyValueSeparator(kvSeparator);
		assertThat(withKeyValueSeparator.join(map)).isEqualTo( //
				"A" + kvSeparator + "42" + separator //
						+ "2" + kvSeparator + "null" + separator //
						+ "3" + kvSeparator + "3");
	}

}
