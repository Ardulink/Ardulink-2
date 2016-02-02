package org.zu.ardulink.util;

import java.util.Enumeration;
import java.util.Iterator;

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

}
