package org.ardulink.util;

import static org.ardulink.util.anno.LapsedWith.JDK8;

import org.ardulink.util.anno.LapsedWith;

@LapsedWith(value = JDK8)
public final class Objects {

	private Objects() {
		super();
	}

	public static boolean equals(Object a, Object b) {
		return a == b || a != null && a.equals(b);
	}

}
