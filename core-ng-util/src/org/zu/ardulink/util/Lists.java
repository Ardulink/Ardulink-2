package org.zu.ardulink.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Lists {

	private Lists() {
		super();
	}

	public static <T> List<T> newArrayList(Iterable<T> iterable) {
		return newArrayList(iterable.iterator());
	}

	public static <T> List<T> newArrayList(Iterator<T> iterator) {
		List<T> list = new ArrayList<T>();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}
		return list;
	}

	public static <T> List<T> newArrayList(T... values) {
		List<T> list = new ArrayList<T>();
		Collections.addAll(list, values);
		return list;
	}

}
