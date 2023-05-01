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

import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.anno.LapsedWith.JDK9;

import java.util.ArrayList;
import java.util.Collections;
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
		List<T> list = new ArrayList<>();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}
		return list;
	}

	public static <T> List<T> newArrayList(T... values) {
		List<T> list = new ArrayList<>();
		Collections.addAll(list, values);
		return list;
	}

	public static <T> T rangeCheckedGet(List<T> list, int index) {
		return rangeCheckedGet(list, index, "index");
	}

	public static <T> T rangeCheckedGet(List<T> list, int index, String name) {
		checkArgument(index >= 0 && index < list.size(), "%s out of range %d >= %d < %d", name, 0, index, list.size());
		return list.get(index);
	}

}
