package org.ardulink.util;

public final class Objects {

	private Objects() {
		super();
	}

	public static boolean equals(Object a, Object b) {
		return a == b || a != null && a.equals(b);
	}

}
