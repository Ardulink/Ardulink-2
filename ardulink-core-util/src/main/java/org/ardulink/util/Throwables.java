package org.ardulink.util;

import static org.ardulink.util.Preconditions.checkNotNull;

import java.util.Iterator;

public final class Throwables {

	private Throwables() {
		super();
	}

	public static Throwable getRootCause(Throwable throwable) {
		return Iterators.getLast(getCauses(throwable)).or(throwable);
	}

	public static Iterator<Throwable> getCauses(final Throwable throwable) {
		return new Iterator<Throwable>() {

			private Throwable actual = throwable;

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Throwable next() {
				return this.actual = this.actual.getCause();
			}

			@Override
			public boolean hasNext() {
				return this.actual.getCause() != null;
			}
		};
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
