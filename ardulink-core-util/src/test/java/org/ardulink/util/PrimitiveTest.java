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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import org.hamcrest.core.Is;
import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class PrimitiveTest {

	@Test
	public void canParseIntAsInt() {
		assertThat(Primitive.parseAs(int.class, "123"),
				Is.<Object> is(Integer.valueOf(123)));
	}

	@Test
	public void canParseIntAsDouble() {
		assertThat(Primitive.parseAs(double.class, "123"),
				Is.<Object> is(Double.valueOf(123)));
	}

	@Test
	public void canParseDoubleAsDouble() {
		assertThat(Primitive.parseAs(double.class, "123.456"),
				Is.<Object> is(Double.valueOf(123.456)));
	}

	@Test(expected = NumberFormatException.class)
	public void cannotParseDoubleAsInt() {
		Primitive.parseAs(int.class, "123.456");
	}

	@Test
	public void testForClassName() {
		assertThat(Primitive.forClassName("int"), is(Primitive.INT));
		assertThat(Primitive.forClassName("double"), is(Primitive.DOUBLE));
		assertThat(Primitive.forClassName(String.class.getName()),
				is(nullValue()));
	}

	@Test
	public void isWrapperTypeForNonWrapperReturnsFalse() {
		assertThat(Primitive.isWrapperType(int.class), is(FALSE));
	}

	@Test
	public void isWrapperTypeForWrapperReturnsTrue() {
		assertThat(Primitive.isWrapperType(Integer.class), is(TRUE));
	}

	@Test
	public void allPrimitiveTypesContainsInt() {
		assertThat(Primitive.allPrimitiveTypes().contains(int.class), is(TRUE));
	}

	@Test
	public void unwrapOnNonWrapperTypeReturnsArgument() {
		assertThat(Primitive.unwrap(String.class).getName(),
				is(String.class.getName()));
	}

	@Test
	public void unwrapOnWrapperTypeReturnsWrappedPrimitive() {
		assertThat(Primitive.unwrap(Integer.class).getName(),
				is(int.class.getName()));
	}

	@Test
	public void wrapOnNonPrimitiveTypeReturnsArgument() {
		assertThat(Primitive.wrap(String.class).getName(),
				is(String.class.getName()));
	}

	@Test
	public void wrapOnPrimitiveTypeReturnsWrappedType() {
		assertThat(Primitive.wrap(int.class).getName(),
				is(Integer.class.getName()));
	}

}
