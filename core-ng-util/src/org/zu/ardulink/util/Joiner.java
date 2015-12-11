package org.zu.ardulink.util;

import java.util.Iterator;

public final class Joiner {

	private final String separator;

	private Joiner(String separator) {
		this.separator = separator;
	}

	public static Joiner on(String separator) {
		return new Joiner(separator);
	}

	public String join(Iterable<Object> values) {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Object> iterator = values.iterator(); iterator.hasNext();) {
			Object next = iterator.next();
			sb = sb.append(next);
			sb = iterator.hasNext() ? sb.append(separator) : sb;
		}
		return sb.toString();
	}

}
