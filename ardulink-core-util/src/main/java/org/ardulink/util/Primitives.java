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

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public enum Primitives {

	INT(Integer.TYPE, Integer.class, s -> Integer.valueOf(s)), //
	BYTE(Byte.TYPE, Byte.class, s -> Byte.valueOf(s)), //
	SHORT(Short.TYPE, Short.class, s -> Short.valueOf(s)), //
	LONG(Long.TYPE, Long.class, s -> Long.valueOf(s)), //
	FLOAT(Float.TYPE, Float.class, s -> Float.valueOf(s)), //
	DOUBLE(Double.TYPE, Double.class, s -> Double.valueOf(s)), //
	BOOLEAN(Boolean.TYPE, Boolean.class, s -> Boolean.valueOf(s)), //
	CHAR(Character.TYPE, Character.class, s -> {
		checkArgument(s.length() == 1, "single character expected but got %s", s);
		return Character.valueOf(s.charAt(0));
	}) {

	};

	private final Class<?> type;
	private final Class<?> wrapperType;
	private final Function<String, Object> parseFunction;

	Primitives(Class<?> type, Class<?> wrapperType, Function<String, Object> parseFunction) {
		this.type = type;
		this.wrapperType = wrapperType;
		this.parseFunction = parseFunction;
	}

	public final Object parse(String value) {
		return parseFunction.apply(value);
	}

	public final Class<?> getWrapperType() {
		return wrapperType;
	}

	public static Object parseAs(Class<?> type, String value) {
		return findPrimitiveFor(type).map(p -> p.parse(value)).orElse(null);
	}

	private static java.util.Optional<Primitives> findPrimitiveFor(Class<?> type) {
		return EnumSet.allOf(Primitives.class).stream().filter(p -> type.isAssignableFrom(p.getType())).findFirst();
	}

	public static Primitives forClassName(String name) {
		return EnumSet.allOf(Primitives.class).stream().filter(p -> p.getType().getName().equals(name)).findFirst()
				.orElse(null);
	}

	public Class<?> getType() {
		return type;
	}

	public static boolean isWrapperType(Class<?> clazz) {
		return EnumSet.allOf(Primitives.class).stream().map(Primitives::getWrapperType).anyMatch(clazz::equals);
	}

	public static Collection<Class<?>> allPrimitiveTypes() {
		return EnumSet.allOf(Primitives.class).stream().map(Primitives::getType).collect(toList());
	}

	public static Class<?> unwrap(Class<?> clazz) {
		return findAndMap(clazz, p -> clazz.equals(p.getWrapperType()), Primitives::getType);
	}

	public static Class<?> wrap(Class<?> clazz) {
		return findAndMap(clazz, p -> clazz.equals(p.getType()), Primitives::getWrapperType);
	}

	private static Class<?> findAndMap(Class<?> clazz, Predicate<? super Primitives> predicate,
			Function<? super Primitives, Class<?>> mapper) {
		Optional<Class<?>> map = EnumSet.allOf(Primitives.class).stream().filter(predicate).findFirst().map(mapper);
		return map.orElse(clazz);
	}

}