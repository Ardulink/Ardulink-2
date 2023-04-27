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
public final class Streams {

	private Streams() {
		super();
	}

	public static <T> Optional<T> getFirst(Stream<T> stream) {
		return stream.findFirst();
	}

	public static <T> Optional<T> getLast(Stream<T> stream) {
		return stream.reduce((first, second) -> second);
	}

	@LapsedWith(module = JDK9, value = "ServiceLoader#stream")
	public static <T> Stream<T> stream(Iterator<T> iterator) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
	}

	public static <T> Stream<T> stream(Iterable<T> iterable) {
		return stream(iterable.iterator());
	}

}
