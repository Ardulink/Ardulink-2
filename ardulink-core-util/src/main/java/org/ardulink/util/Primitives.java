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

import static java.util.stream.Collectors.toList;
import static org.ardulink.util.Preconditions.checkArgument;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public enum Primitives {

	INT(Integer.TYPE, Integer.class, Integer::valueOf, (int) 0), //
	BYTE(Byte.TYPE, Byte.class, Byte::valueOf, (byte) 0), //
	SHORT(Short.TYPE, Short.class, Short::valueOf, (short) 0), //
	LONG(Long.TYPE, Long.class, Long::valueOf, (long) 0), //
	FLOAT(Float.TYPE, Float.class, Float::valueOf, (float) 0), //
	DOUBLE(Double.TYPE, Double.class, Double::valueOf, (double) 0), //
	BOOLEAN(Boolean.TYPE, Boolean.class, Boolean::valueOf, false), //
	CHAR(Character.TYPE, Character.class, Primitives::charValueOfHelper, (char) 0);

	private static char charValueOfHelper(String string) {
		checkArgument(string.length() == 1, "single character expected but got %s", string);
		return Character.valueOf(string.charAt(0));
	}

	private final Class<?> type;
	private final Class<?> wrapperType;
	private final Function<String, Object> parseFunction;
	private final Object defaultValue;

	private Primitives(Class<?> type, Class<?> wrapperType, Function<String, Object> parseFunction, Object defaultValue) {
		this.type = type;
		this.wrapperType = wrapperType;
		this.parseFunction = parseFunction;
		this.defaultValue = defaultValue;
	}

	public final Object parse(String value) {
		return parseFunction.apply(value);
	}

	public final Class<?> getWrapperType() {
		return wrapperType;
	}
	
	public Object defaultValue() {
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseAs(Class<T> type, String value) {
		return (T) wrap(type).cast(findPrimitiveFor(unwrap(type)).map(p -> p.parse(value)).orElse(null));
	}

	public static <T> Optional<T> tryParseAs(Class<T> type, String value) {
		try {
			return Optional.ofNullable(parseAs(type, value));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public static Optional<Primitives> findPrimitiveFor(Class<?> type) {
		return primitiveMatching(p -> type.isAssignableFrom(p.getType()));
	}

	/**
	 * Returns the primitive for the passed primitive class name. If the passed in
	 * class name isn't a primitive class <code>null</code> is returned.
	 * 
	 * @param name class name to find the primitive for
	 * @return primitive matching or null if not a primitive
	 */
	public static Primitives forClassName(String name) {
		return primitiveMatching(p -> p.getType().getName().equals(name)).orElse(null);
	}

	private static Optional<Primitives> primitiveMatching(Predicate<? super Primitives> predicate) {
		return streamOfAll().filter(predicate).findFirst();
	}

	private static Stream<Primitives> streamOfAll() {
		return EnumSet.allOf(Primitives.class).stream();
	}

	public Class<?> getType() {
		return type;
	}

	public static boolean isWrapperType(Class<?> clazz) {
		return streamOfAll().map(Primitives::getWrapperType).anyMatch(clazz::equals);
	}

	public static Collection<Class<?>> allPrimitiveTypes() {
		return streamOfAll().map(Primitives::getType).collect(toList());
	}

	/**
	 * Returns the corresponding primitive for the passed wrapper. If the passed in
	 * class isn't a wrapper type the passed in class is returned.
	 * 
	 * @param clazz wrapper class
	 * @return primitive type
	 */
	public static Class<?> unwrap(Class<?> clazz) {
		return findAndMap(clazz, p -> clazz.equals(p.getWrapperType()), Primitives::getType);
	}

	/**
	 * Returns the corresponding wrapper for the passed primitive. If the passed in
	 * class isn't a primitive type the passed in class is returned.
	 * 
	 * @param clazz primitive class
	 * @return wrapper type
	 */
	public static Class<?> wrap(Class<?> clazz) {
		return findAndMap(clazz, p -> clazz.equals(p.getType()), Primitives::getWrapperType);
	}

	private static Class<?> findAndMap(Class<?> clazz, Predicate<? super Primitives> predicate,
			Function<? super Primitives, Class<?>> mapper) {
		return primitiveMatching(predicate).map(mapper).orElse(clazz);
	}

}