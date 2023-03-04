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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
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
class OptionalTest {

	@Test
	void optionalFromNullableWithNullValueIsNotPresent() {
		assertThat(Optional.ofNullable(null).isPresent(), is(FALSE));
	}

	@Test
	void optionalFromNullableWithNonNullValueIsPresent() {
		assertThat(Optional.ofNullable("foo").isPresent(), is(TRUE));
	}

	@Test
	void getOnNonPresentOptionalThrowsRTE() {
		assertThrows(RuntimeException.class, () -> Optional.ofNullable(null).get());
	}

	@Test
	void getOnPresentOptionalReturnsObject() {
		assertThat(Optional.ofNullable("foo").get(), is("foo"));
	}

	@Test
	void canCreateInstanceWithNonNullValue() {
		assertThat(Optional.of("foo").get(), is("foo"));
	}

	@Test
	void callingOfWithNullValueThrowsRTE() {
		assertThrows(RuntimeException.class, () -> Optional.of(null));
	}

	@Test
	void doesReturnExistingValueIfQueryingAlternative() {
		assertThat(Optional.ofNullable("foo").orElse("bar"), is("foo"));
	}

	@Test
	void doesReturnAlternativeIfQueryingAlternative() {
		assertThat(Optional.<String>absent().orElse("bar"), is("bar"));
	}

	@Test
	void getOrThrowThrowsIllegalStateExceptionOnAbsentOptionals() {
		String text = "exception text";
		assertThat(assertThrows(IllegalStateException.class, () -> Optional.<String>absent().getOrThrow(text))
				.getMessage(), is(text));
	}

	@Test
	void getOrThrowReturnsValueOnPresentOptionals() {
		assertThat(Optional.of("foo").getOrThrow("exception text"), is("foo"));
	}

	@Test
	void canThrowIndividualExceptions() {
		String text = "exception text";
		assertThat(
				assertThrows(RuntimeException.class,
						() -> Optional.<String>absent().getOrThrow(IllegalArgumentException.class, text)).getMessage(),
				is(text));
	}

	@Test
	void doesBreakWithNiceMessageIfExceptionClassDoesNotHaveAstringConstructor() {
		class MyRTEWithoutAstringConstructor extends RuntimeException {
			private static final long serialVersionUID = 1L;
		}
		RuntimeException exception = assertThrows(RuntimeException.class, () -> Optional.<String>absent()
				.getOrThrow(MyRTEWithoutAstringConstructor.class, "no matter what typed here"));
		assertThat(exception.getMessage(), is(allOf(containsString(MyRTEWithoutAstringConstructor.class.getName()),
				containsString("not"), containsString("String constructor"))));
	}

}
