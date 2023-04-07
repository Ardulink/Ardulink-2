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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		assertThat(Primitives.parseAs(int.class, "123")).isEqualTo(Integer.valueOf(123));
	}

	@Test
	void canParseIntAsDouble() {
		assertThat(Primitives.parseAs(double.class, "123")).isEqualTo(Double.valueOf(123));
	}

	@Test
	void canParseDoubleAsDouble() {
		assertThat(Primitives.parseAs(double.class, "123.456")).isEqualTo(Double.valueOf(123.456));
	}

	@Test
	void cannotParseDoubleAsInt() {
		assertThrows(NumberFormatException.class, () -> {
			Primitives.parseAs(int.class, "123.456");
		});
	}

	@Test
	void testForClassName() {
		assertThat(Primitives.forClassName("int")).isEqualTo(Primitives.INT);
		assertThat(Primitives.forClassName("double")).isEqualTo(Primitives.DOUBLE);
		assertThat(Primitives.forClassName(String.class.getName())).isNull();
	}

	@Test
	void isWrapperTypeForNonWrapperReturnsFalse() {
		assertThat(Primitives.isWrapperType(int.class)).isEqualTo(FALSE);
	}

	@Test
	void isWrapperTypeForWrapperReturnsTrue() {
		assertThat(Primitives.isWrapperType(Integer.class)).isEqualTo(TRUE);
	}

	@Test
	void allPrimitiveTypesContainsInt() {
		assertThat(Primitives.allPrimitiveTypes().contains(int.class)).isEqualTo(TRUE);
	}

	@Test
	void unwrapOnNonWrapperTypeReturnsArgument() {
		assertThat(Primitives.unwrap(String.class).getName()).isEqualTo(String.class.getName());
	}

	@Test
	void unwrapOnWrapperTypeReturnsWrappedPrimitive() {
		assertThat(Primitives.unwrap(Integer.class).getName()).isEqualTo(int.class.getName());
	}

	@Test
	void wrapOnNonPrimitiveTypeReturnsArgument() {
		assertThat(Primitives.wrap(String.class).getName()).isEqualTo(String.class.getName());
	}

	@Test
	void wrapOnPrimitiveTypeReturnsWrappedType() {
		assertThat(Primitives.wrap(int.class).getName()).isEqualTo(Integer.class.getName());
	}

}
