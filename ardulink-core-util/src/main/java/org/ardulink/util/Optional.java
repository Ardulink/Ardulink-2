/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.util;

import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class Optional<T> {

	private static class PresentOptional<T> extends Optional<T> {

		private final T value;

		public PresentOptional(T value) {
			this.value = value;
		}

		@Override
		public boolean isPresent() {
			return true;
		}

		@Override
		public T get() {
			return value;
		}

		@Override
		public T or(T other) {
			return value;
		}

		@Override
		public String toString() {
			return "Optional.of(" + value + ")";
		}

	}

	private static final class AbsentOptional extends Optional<Object> {

		@Override
		public boolean isPresent() {
			return false;
		}

		@Override
		public Object get() {
			throw new IllegalStateException("absent");
		}

		@Override
		public Object or(Object other) {
			return other;
		}

		public String toString() {
			return "Optional.absent";
		}
	}

	private static final Optional<Object> absent = new AbsentOptional();

	private Optional() {
		super();
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<T> absent() {
		return (Optional<T>) absent;
	}

	public static <T> Optional<T> ofNullable(final T object) {
		return object == null ? Optional.<T> absent() : new PresentOptional<T>(
				object);
	}

	public static <T> Optional<T> of(T object) {
		return new PresentOptional<T>(checkNotNull(object,
				"value must not be null"));
	}

	public abstract boolean isPresent();

	public abstract T get();

	public abstract T or(T other);

	public T orNull() {
		return or(null);
	}

	/**
	 * Returns the optional's value if present. Throws a RuntimeException if the
	 * optional if absent.
	 * 
	 * @param message
	 *            message of RuntimeException including placeholders
	 * @param args
	 *            placeholder values
	 * @return optional's value
	 */
	public T getOrThrow(String message, Object... args) {
		checkState(isPresent(), message, args);
		return get();
	}

	/**
	 * Returns the optional's value if present. Throws a RuntimeException of the
	 * passed type if the optional if absent.
	 * 
	 * @param exceptionClass
	 *            type to create new exception. The class has to define a public
	 *            String constructor.
	 * @param message
	 *            message of RuntimeException including placeholders
	 * @param args
	 *            placeholder values
	 * @return optional's value
	 */
	public T getOrThrow(Class<? extends RuntimeException> exceptionClass,
			String message, Object... args) {
		if (isPresent()) {
			return get();
		}
		throw newException(exceptionClass, String.format(message, args));
	}

	private <E> E newException(Class<E> exceptionClass, String message) {
		try {
			return findConstructor(exceptionClass, String.class).getOrThrow(
					"Class %s does not define a String constructor",
					exceptionClass).newInstance(message);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <E> Optional<Constructor<E>> findConstructor(
			Class<E> exceptionClass, Class<?>... types)
			throws NoSuchMethodException {
		for (Constructor<?> constructor : exceptionClass.getConstructors()) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (Arrays.equals(types, parameterTypes)) {
				return Optional.of((Constructor<E>) constructor);
			}
		}
		return Optional.absent();
	}

}
