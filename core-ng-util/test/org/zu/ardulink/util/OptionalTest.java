package org.zu.ardulink.util;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OptionalTest {

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

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
		exceptions.expect(RuntimeException.class);
		Optional.ofNullable(null).get();
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
		exceptions.expect(RuntimeException.class);
		Optional.of(null);
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
		exceptions.expect(IllegalStateException.class);
		exceptions.expectMessage("exception text");
		Optional.<String> absent().getOrThrow("exception text");
	}

	@Test
	public void getOrThrowReturnsValueOnPresentOptionals() {
		assertThat(Optional.of("foo").getOrThrow("exception text"), is("foo"));
	}

	@Test
	public void canThrowIndividualExceptions() {
		exceptions.expect(IllegalArgumentException.class);
		exceptions.expectMessage("exception text");
		Optional.<String> absent().getOrThrow(IllegalArgumentException.class,
				"exception text");
	}

	@Test
	public void doesBreakWithNiceMessageIfExceptionClassDoesNotHaveAstringConstructor() {
		class MyRTEWithoutAstringConstructor extends RuntimeException {
			private static final long serialVersionUID = 1L;
		}
		exceptions.expect(RuntimeException.class);
		exceptions.expectMessage(allOf(
				containsString(MyRTEWithoutAstringConstructor.class.getName()),
				containsString("not"), containsString("String constructor")));
		Optional.<String> absent().getOrThrow(
				MyRTEWithoutAstringConstructor.class,
				"no matter what typed here");
	}
}
