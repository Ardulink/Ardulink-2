package org.ardulink.util;

import static java.util.stream.Stream.iterate;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Streams.getLast;

import java.util.stream.Stream;

public final class Throwables {

	private Throwables() {
		super();
	}

	public static Throwable getRootCause(Throwable throwable) {
		return getLast(getCauses(throwable)).orElse(throwable);
	}

	public static Stream<Throwable> getCauses(Throwable throwable) {
		return iterate(throwable, Throwable::getCause).filter(t -> t.getCause() == null);
	}

	public static RuntimeException propagate(Throwable throwable) {
		propagateIfPossible(checkNotNull(throwable, "throwable must not be null"));
		throw new RuntimeException(throwable);
	}

	public static void propagateIfPossible(Throwable throwable) {
		propagateIfInstanceOf(throwable, Error.class);
		propagateIfInstanceOf(throwable, RuntimeException.class);
	}

	public static <T extends Throwable> void propagateIfInstanceOf(Throwable throwable, Class<T> type) throws T {
		if (type.isInstance(throwable)) {
			throw type.cast(throwable);
		}
	}

}
