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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

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
		assertThatThrownBy(() -> Primitives.parseAs(Integer.class, "123.456"))
				.isInstanceOf(NumberFormatException.class);
		assertThatThrownBy(() -> Primitives.parseAs(int.class, "123.456")).isInstanceOf(NumberFormatException.class);

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

	@Test
	void findPrimitiveFor() {
		assertThat(Primitives.findPrimitiveFor(Boolean.class)).isEmpty();
		assertThat(Primitives.findPrimitiveFor(String.class)).isEmpty();

		assertThat(Primitives.findPrimitiveFor(boolean.class)).hasValue(Primitives.BOOLEAN);
		assertThat(Primitives.findPrimitiveFor(byte.class)).hasValue(Primitives.BYTE);
		assertThat(Primitives.findPrimitiveFor(char.class)).hasValue(Primitives.CHAR);
		assertThat(Primitives.findPrimitiveFor(double.class)).hasValue(Primitives.DOUBLE);
		assertThat(Primitives.findPrimitiveFor(float.class)).hasValue(Primitives.FLOAT);
		assertThat(Primitives.findPrimitiveFor(int.class)).hasValue(Primitives.INT);
		assertThat(Primitives.findPrimitiveFor(long.class)).hasValue(Primitives.LONG);
		assertThat(Primitives.findPrimitiveFor(short.class)).hasValue(Primitives.SHORT);
	}

	@Test
	void defaults() {
		assertThat(Primitives.BOOLEAN.defaultValue()).isEqualTo(false);
		assertThat(Primitives.BYTE.defaultValue()).isEqualTo((byte) 0);
		assertThat(Primitives.CHAR.defaultValue()).isEqualTo((char) 0);
		assertThat(Primitives.DOUBLE.defaultValue()).isEqualTo((double) 0);
		assertThat(Primitives.FLOAT.defaultValue()).isEqualTo((float) 0);
		assertThat(Primitives.INT.defaultValue()).isEqualTo((int) 0);
		assertThat(Primitives.LONG.defaultValue()).isEqualTo((long) 0);
		assertThat(Primitives.SHORT.defaultValue()).isEqualTo((short) 0);
	}

}
