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

import static java.util.stream.Stream.empty;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;

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

	public static <T> Optional<T> getLast(Stream<T> stream) {
		return stream.reduce((first, second) -> second);
	}

	public static <T> Iterable<T> iterable(Stream<T> stream) {
		return () -> iterator(stream);
	}

	public static <T> Iterator<T> iterator(Stream<T> stream) {
		return Spliterators.iterator(stream.spliterator());
	}

	@SafeVarargs
	public static <T> Stream<T> concat(Stream<T>... streams) {
		return Arrays.stream(streams).reduce(Stream::concat).orElse(empty());
	}

}
