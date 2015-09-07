package com.github.pfichtner.ardulink.util;

public final class Preconditions {

	private Preconditions() {
		super();
	}

	public static void checkArgument(boolean state, String message,
			Object... args) {
		if (!state) {
			throw new IllegalArgumentException(String.format(message, args));
		}
	}

}
