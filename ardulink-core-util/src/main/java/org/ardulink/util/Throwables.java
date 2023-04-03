package org.ardulink.util;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Streams.getLast;

import java.util.stream.Stream;

import org.ardulink.util.anno.LapsedWith;

public final class Throwables {

	private Throwables() {
		super();
	}

	public static Throwable getRootCause(Throwable throwable) {
		return getLast(getCauses(throwable)).orElse(throwable);
	}

	public static Stream<Throwable> getCauses(Throwable throwable) {
		return _getCauses(throwable);
	}

	@LapsedWith(module = LapsedWith.JDK9, value = "Stream#iterate(throwable, Objects::nonNull, Throwable::getCause)")
	private static Stream<Throwable> _getCauses(Throwable throwable) {
		return concat(Stream.of(throwable), throwable.getCause() == null ? empty() : _getCauses(throwable.getCause()));
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
