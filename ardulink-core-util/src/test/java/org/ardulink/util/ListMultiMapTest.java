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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ListMultiMapTest {

	@Test
	void iteratorOnEmpty() {
		ListMultiMap<Integer, String> sut = new ListMultiMap<>();
		Iterator<Entry<Integer, String>> iterator = sut.iterator();
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	void iteratorOnSingleElement() {
		ListMultiMap<Integer, String> sut = new ListMultiMap<>();
		sut.put(1, "foo");
		Iterator<Entry<Integer, String>> iterator = sut.iterator();
		assertThat(iterator.hasNext(), is(true));
		Entry<Integer, String> next = iterator.next();
		assertThat(next.getKey(), is(1));
		assertThat(next.getValue(), is("foo"));
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	void iteratorOnCollisionElement() {
		ListMultiMap<Integer, String> sut = new ListMultiMap<>();
		sut.put(1, "foo");
		sut.put(1, "bar");
		Iterator<Entry<Integer, String>> iterator = sut.iterator();
		assertThat(iterator.hasNext(), is(true));
		Entry<Integer, String> next = iterator.next();
		assertThat(next.getKey(), is(1));
		assertThat(next.getValue(), is("foo"));
		next = iterator.next();
		assertThat(next.getKey(), is(1));
		assertThat(next.getValue(), is("bar"));
		assertThat(iterator.hasNext(), is(false));
	}

}
