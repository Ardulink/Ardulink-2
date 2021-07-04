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
import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThrows;

import org.ardulink.util.anno.LapsedWith;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class OptionalTest {

	@Test
	public void optionalFromNullableWithNullValueIsNotPresent() {
		assertThat(Optional.ofNullable(null).isPresent(), is(FALSE));
	}

	@Test
	public void optionalFromNullableWithNonNullValueIsPresent() {
		assertThat(Optional.ofNullable("foo").isPresent(), is(TRUE));
	}

	@Test
	public void getOnNonPresentOptionalThrowsRTE() {
		@LapsedWith(module = JDK8, value = "Lambda")
		ThrowingRunnable runnable = new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				Optional.ofNullable(null).get();
			}
		};
		assertThrows(RuntimeException.class, runnable);
	}

	@Test
	public void getOnPresentOptionalReturnsObject() {
		assertThat(Optional.ofNullable("foo").get(), is("foo"));
	}

	@Test
	public void canCreateInstanceWithNonNullValue() {
		assertThat(Optional.of("foo").get(), is("foo"));
	}

	@Test
	public void callingOfWithNullValueThrowsRTE() {
		@LapsedWith(module = JDK8, value = "Lambda")
		ThrowingRunnable runnable = new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				Optional.of(null);
			}
		};
		assertThrows(RuntimeException.class, runnable);

	}

	@Test
	public void doesReturnExistingValueIfQueryingAlternative() {
		assertThat(Optional.ofNullable("foo").or("bar"), is("foo"));
	}

	@Test
	public void doesReturnAlternativeIfQueryingAlternative() {
		assertThat(Optional.<String> absent().or("bar"), is("bar"));
	}

	@Test
	public void getOrThrowThrowsIllegalStateExceptionOnAbsentOptionals() {
		@LapsedWith(module = JDK8, value = "Lambda")
		IllegalStateException exception = assertThrows(IllegalStateException.class, new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				Optional.<String> absent().getOrThrow("exception text");
			}
		});
		assertThat(exception.getMessage(), is("exception text"));
	}

	@Test
	public void getOrThrowReturnsValueOnPresentOptionals() {
		assertThat(Optional.of("foo").getOrThrow("exception text"), is("foo"));
	}

	@Test
	public void canThrowIndividualExceptions() {
		@LapsedWith(module = JDK8, value = "Lambda")
		RuntimeException exception = assertThrows(RuntimeException.class, new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				Optional.<String> absent().getOrThrow(IllegalArgumentException.class,
						"exception text");
			}
		});
		assertThat(exception.getMessage(), is("exception text"));
	}

	@Test
	public void doesBreakWithNiceMessageIfExceptionClassDoesNotHaveAstringConstructor() {
		class MyRTEWithoutAstringConstructor extends RuntimeException {
			private static final long serialVersionUID = 1L;
		}
		@LapsedWith(module = JDK8, value = "Lambda")
		RuntimeException exception = assertThrows(RuntimeException.class, new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				Optional.<String> absent().getOrThrow(
						MyRTEWithoutAstringConstructor.class,
						"no matter what typed here");
			}
		});
		assertThat(exception.getMessage(), is(allOf(
				containsString(MyRTEWithoutAstringConstructor.class.getName()),
				containsString("not"), containsString("String constructor"))));
	}
}
