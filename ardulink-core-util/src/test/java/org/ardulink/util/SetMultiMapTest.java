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

package org.zu.ardulink.util;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class SetMultiMapTest {

	@Test
	public void canPut() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.asMap(), is(buildMap(1, Collections.singleton("foo"))));
	}

	@Test
	public void canPutTwice() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.put(1, "foo"), is(FALSE));
		assertThat(s.asMap(), is(buildMap(1, Collections.singleton("foo"))));
	}

	@Test
	public void canRemoveExistingValue() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.remove(1, "foo"), is(TRUE));
		assertThat(s.asMap(), is(Collections.<Integer, Set<String>> emptyMap()));
	}

	@Test
	public void canHandleRemovesOfNonExistingValues() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.remove(1, "bar"), is(FALSE));
		assertThat(s.asMap(), is(buildMap(1, Collections.singleton("foo"))));
	}

	@Test
	public void canHandleRemovesOfNonExistingKeys() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.remove(2, "foo"), is(FALSE));
		assertThat(s.asMap(), is(buildMap(1, Collections.singleton("foo"))));
	}

	private static Map<Integer, Set<String>> buildMap(Integer key,
			Set<String> value) {
		Map<Integer, Set<String>> m = new HashMap<Integer, Set<String>>();
		m.put(key, value);
		return m;
	}

}
