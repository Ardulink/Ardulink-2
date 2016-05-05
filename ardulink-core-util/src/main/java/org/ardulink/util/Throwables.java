package org.ardulink.util;

import static org.ardulink.util.Preconditions.checkNotNull;

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

	public static RuntimeException propagate(Throwable throwable) {
		propagateIfPossible(checkNotNull(throwable,
				"throwable must not be null"));
		throw new RuntimeException(throwable);
	}

	public static void propagateIfPossible(Throwable throwable) {
		propagateIfInstanceOf(throwable, Error.class);
		propagateIfInstanceOf(throwable, RuntimeException.class);
	}

	public static <T extends Throwable> void propagateIfInstanceOf(
			Throwable throwable, Class<T> type) throws T {
		if (type.isInstance(throwable)) {
			throw type.cast(throwable);
		}
	}

}
