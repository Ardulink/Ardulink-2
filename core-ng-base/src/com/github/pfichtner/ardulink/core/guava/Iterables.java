package com.github.pfichtner.ardulink.core.guava;

import java.util.Enumeration;
import java.util.Iterator;

public final class Iterables {

	private Iterables() {
		super();
	}

	public static <T> Iterable<T> forEnumeration(
			final Enumeration<T> enumeration) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return Iterators.forEnumeration(enumeration);
			}
		};
	}

}
