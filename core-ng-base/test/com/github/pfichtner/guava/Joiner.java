package com.github.pfichtner.guava;

import java.util.Iterator;

public final class Joiner {

	private String separator;

	private Joiner(String separator) {
		this.separator = separator;
	}

	public static Joiner on(String separator) {
		return new Joiner(separator);
	}

	public String join(Iterable<Object> iterable) {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Object> iterator = iterable.iterator(); iterator
				.hasNext();) {
			sb = sb.append(iterator.next());
			if (iterator.hasNext()) {
				sb = sb.append(this.separator);
			}
		}
		return sb.toString();
	}

}
