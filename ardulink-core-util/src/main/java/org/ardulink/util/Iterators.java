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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Iterators {

	public static class EnumerationIteratorAdapter<T> implements Iterator<T> {

		private final Enumeration<T> enumeration;

		public EnumerationIteratorAdapter(Enumeration<T> enumeration) {
			this.enumeration = enumeration;
		}

		@Override
		public boolean hasNext() {
			return enumeration.hasMoreElements();
		}

		@Override
		public T next() {
			return enumeration.nextElement();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private Iterators() {
		super();
	}

	public static <T> Iterator<T> forEnumeration(
			final Enumeration<T> enumeration) {
		return new EnumerationIteratorAdapter<T>(enumeration);
	}

	public static <T> Optional<T> getFirst(Iterator<T> iterator) {
		return iterator.hasNext() ? Optional.<T> of(iterator.next()) : Optional
				.<T> absent();
	}

	public static <T> Optional<T> getLast(Iterator<T> iterator) {
		if (!iterator.hasNext()) {
			return Optional.absent();
		}
		T last;
		do {
			last = iterator.next();
		} while (iterator.hasNext());
		return Optional.of(last);
	}

}
