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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singleton;
import static org.ardulink.util.Maps.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	@Test
	void canPut() {
		assertThat(sut.put(1, "foo")).isEqualTo(TRUE);
		assertThat(sut.asMap()).containsExactly(entry(1, singleton("foo")));
	}

	@Test
	void canPutTwice() {
		assertThat(sut.put(1, "foo")).isEqualTo(TRUE);
		assertThat(sut.put(1, "foo")).isEqualTo(FALSE);
		assertThat(sut.asMap()).containsExactly(entry(1, singleton("foo")));
	}

	@Test
	void canRemoveExistingValue() {
		assertThat(sut.put(1, "foo")).isEqualTo(TRUE);
		assertThat(sut.remove(1, "foo")).isEqualTo(TRUE);
		assertThat(sut.asMap()).isEmpty();
	}

	@Test
	void canHandleRemovesOfNonExistingValues() {
		assertThat(sut.put(1, "foo")).isEqualTo(TRUE);
		assertThat(sut.remove(1, "bar")).isEqualTo(FALSE);
		assertThat(sut.asMap()).containsExactly(entry(1, singleton("foo")));
	}

	@Test
	void canHandleRemovesOfNonExistingKeys() {
		assertThat(sut.put(1, "foo")).isEqualTo(TRUE);
		assertThat(sut.remove(2, "foo")).isEqualTo(FALSE);
		assertThat(sut.asMap()).containsExactly(entry(1, singleton("foo")));
	}

}
