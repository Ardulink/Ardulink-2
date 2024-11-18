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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class PrimitivesTest {

	@Test
	void canParseIntAsInt() {
		assertThat(Primitives.parseAs(Integer.class, "123")).isEqualTo(Integer.valueOf(123));
		assertThat(Primitives.parseAs(int.class, "123")).isEqualTo(Integer.valueOf(123));
	}

	@Test
	void canParseIntAsDouble() {
		assertThat(Primitives.parseAs(Double.class, "123")).isEqualTo(Double.valueOf(123));
		assertThat(Primitives.parseAs(double.class, "123")).isEqualTo(Double.valueOf(123));
	}

	@Test
	void canParseDoubleAsDouble() {
		assertThat(Primitives.parseAs(Double.class, "123.456")).isEqualTo(Double.valueOf(123.456));
		assertThat(Primitives.parseAs(double.class, "123.456")).isEqualTo(Double.valueOf(123.456));
	}

	@Test
	void cannotParseDoubleAsInt() {
		assertThatExceptionOfType(NumberFormatException.class)
				.isThrownBy(() -> Primitives.parseAs(Integer.class, "123.456"));
		assertThatExceptionOfType(NumberFormatException.class)
				.isThrownBy(() -> Primitives.parseAs(int.class, "123.456"));
	}

	@Test
	void testForClassName() {
		assertThat(Primitives.forClassName("int")).isEqualTo(Primitives.INT);
		assertThat(Primitives.forClassName("double")).isEqualTo(Primitives.DOUBLE);
		assertThat(Primitives.forClassName(String.class.getName())).isNull();
	}

	@Test
	void isWrapperTypeForNonWrapperReturnsFalse() {
		assertThat(Primitives.isWrapperType(int.class)).isFalse();
	}

	@Test
	void isWrapperTypeForWrapperReturnsTrue() {
		assertThat(Primitives.isWrapperType(Integer.class)).isTrue();
	}

	@Test
	void allPrimitiveTypesContainsAllPrimitiveTypes() {
		assertThat(Primitives.allPrimitiveTypes()).containsExactlyInAnyOrder(boolean.class, byte.class, int.class,
				long.class, short.class, double.class, float.class, char.class);
	}

	@Test
	void unwrapOnNonWrapperTypeReturnsArgument() {
		assertThat(Primitives.unwrap(String.class)).isEqualTo(String.class);
	}

	@Test
	void unwrapOnWrapperTypeReturnsWrappedPrimitive() {
		assertThat(Primitives.unwrap(Integer.class)).isEqualTo(int.class);
	}

	@Test
	void wrapOnNonPrimitiveTypeReturnsArgument() {
		assertThat(Primitives.wrap(String.class)).isEqualTo(String.class);
	}

	@Test
	void wrapOnPrimitiveTypeReturnsWrappedType() {
		assertThat(Primitives.wrap(int.class)).isEqualTo(Integer.class);
	}

	@ParameterizedTest
	@ValueSource(classes = { Boolean.class, String.class })
	void noPrimitiveForNonPrimitives(Class<?> clazz) {
		assertThat(Primitives.findPrimitiveFor(clazz)).isEmpty();
	}

	@ParameterizedTest
	@MethodSource("primitives")
	void findPrimitiveFor(Primitives value, Object __, Class<?> type) {
		assertThat(Primitives.findPrimitiveFor(type)).hasValue(value);
	}

	@ParameterizedTest
	@MethodSource("primitives")
	void defaults(Primitives value, Object defaultValue, Class<?> __) {
		assertThat(value.defaultValue()).isEqualTo(defaultValue);
	}

	static Stream<Arguments> primitives() {
		return Stream.of( //
				arguments(Primitives.BOOLEAN, false, boolean.class), //
				arguments(Primitives.BYTE, (byte) 0, byte.class), //
				arguments(Primitives.CHAR, (char) 0, char.class), //
				arguments(Primitives.DOUBLE, (double) 0, double.class), //
				arguments(Primitives.FLOAT, (float) 0, float.class), //
				arguments(Primitives.INT, (int) 0, int.class), //
				arguments(Primitives.LONG, (long) 0, long.class), //
				arguments(Primitives.SHORT, (short) 0, short.class) //
		);
	}

}
