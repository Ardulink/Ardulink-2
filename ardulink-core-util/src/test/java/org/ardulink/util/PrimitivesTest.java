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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class PrimitivesTest {

	@Test
	public void canParseIntAsInt() {
		assertThat(Primitives.parseAs(int.class, "123"),
				is((Object) Integer.valueOf(123)));
	}

	@Test
	public void canParseIntAsDouble() {
		assertThat(Primitives.parseAs(double.class, "123"),
				is((Object) Double.valueOf(123)));
	}

	@Test
	public void canParseDoubleAsDouble() {
		assertThat(Primitives.parseAs(double.class, "123.456"),
				is((Object) Double.valueOf(123.456)));
	}

	@Test(expected = NumberFormatException.class)
	public void cannotParseDoubleAsInt() {
		Primitives.parseAs(int.class, "123.456");
	}

	@Test
	public void testForClassName() {
		assertThat(Primitives.forClassName("int"), is(Primitives.INT));
		assertThat(Primitives.forClassName("double"), is(Primitives.DOUBLE));
		assertThat(Primitives.forClassName(String.class.getName()),
				is(nullValue()));
	}

	@Test
	public void isWrapperTypeForNonWrapperReturnsFalse() {
		assertThat(Primitives.isWrapperType(int.class), is(FALSE));
	}

	@Test
	public void isWrapperTypeForWrapperReturnsTrue() {
		assertThat(Primitives.isWrapperType(Integer.class), is(TRUE));
	}

	@Test
	public void allPrimitiveTypesContainsInt() {
		assertThat(Primitives.allPrimitiveTypes().contains(int.class), is(TRUE));
	}

	@Test
	public void unwrapOnNonWrapperTypeReturnsArgument() {
		assertThat(Primitives.unwrap(String.class).getName(),
				is(String.class.getName()));
	}

	@Test
	public void unwrapOnWrapperTypeReturnsWrappedPrimitive() {
		assertThat(Primitives.unwrap(Integer.class).getName(),
				is(int.class.getName()));
	}

	@Test
	public void wrapOnNonPrimitiveTypeReturnsArgument() {
		assertThat(Primitives.wrap(String.class).getName(),
				is(String.class.getName()));
	}

	@Test
	public void wrapOnPrimitiveTypeReturnsWrappedType() {
		assertThat(Primitives.wrap(int.class).getName(),
				is(Integer.class.getName()));
	}

}
