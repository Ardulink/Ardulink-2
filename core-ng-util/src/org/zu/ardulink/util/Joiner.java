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

	public String join(Iterable<? extends Object> values) {
		Iterator<? extends Object> iterator = values.iterator();
		if (!iterator.hasNext()) {
			return "";
		}
		StringBuilder sb = new StringBuilder().append(iterator.next());
		while (iterator.hasNext()) {
			sb = sb.append(separator).append(iterator.next());
		}
		return sb.toString();
	}

}
