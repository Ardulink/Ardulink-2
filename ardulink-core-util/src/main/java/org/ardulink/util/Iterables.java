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

import java.util.Optional;
import java.util.stream.Stream;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Iterables {

	private Iterables() {
		super();
	}

	public static <T> Optional<T> getFirst(Iterable<T> iterable) {
		return Iterators.getFirst(iterable.iterator());
	}

	public static Optional<Integer> getLast(Iterable<Integer> iterable) {
		return Iterators.getLast(iterable.iterator());
	}

	public static <T> Stream<T> stream(Iterable<T> iterable) {
		return Iterators.stream(iterable.iterator());
	}

}
