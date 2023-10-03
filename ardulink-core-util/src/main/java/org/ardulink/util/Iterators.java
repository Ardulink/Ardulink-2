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

import static org.ardulink.util.anno.LapsedWith.JDK9;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Iterators {

	private Iterators() {
		super();
	}

	public static <T> Optional<T> getFirst(Iterator<T> iterator) {
		return iterator.hasNext() ? Optional.ofNullable(iterator.next()) : Optional
				.empty();
	}

	public static <T> Optional<T> getLast(Iterator<T> iterator) {
		if (!iterator.hasNext()) {
			return Optional.empty();
		}
		T last;
		do {
			last = iterator.next();
		} while (iterator.hasNext());
		return Optional.of(last);
	}

	@LapsedWith(module = JDK9, value = "ServiceLoader#stream")
	public static <T> Stream<T> stream(Iterator<T> iterator) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
	}

}
