package org.zu.ardulink.util;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Test;

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

}
