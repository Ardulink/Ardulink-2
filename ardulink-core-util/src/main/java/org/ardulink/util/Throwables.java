package org.ardulink.util;

public final class Throwables {

	private Throwables() {
		super();
	}

	public static Throwable getRootCause(Throwable throwable) {
		Throwable cause;
		while ((cause = throwable.getCause()) != null) {
			throwable = cause;
		}
		return throwable;
	}

}
