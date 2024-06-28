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

import static java.util.Arrays.asList;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.ardulink.util.anno.LapsedWith.JDK9;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@LapsedWith(value = JDK9, module = "List#of")
public final class Lists {

	private Lists() {
		super();
	}

	public static <T> List<T> newArrayList(Iterable<T> iterable) {
		return newArrayList(iterable.iterator());
	}

	public static <T> List<T> newArrayList(Iterator<T> iterator) {
		return stream(spliteratorUnknownSize(iterator, ORDERED), false).collect(toList());
	}

	@SafeVarargs
	public static <T> List<T> newArrayList(T... values) {
		return new ArrayList<>(asList(values));
	}

}
