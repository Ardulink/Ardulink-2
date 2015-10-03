package org.zu.ardulink.util;

import static org.junit.Assert.assertThat;
import static org.zu.ardulink.util.Primitive.parseAs;

import org.hamcrest.core.Is;
import org.junit.Test;

public class PrimitiveTest {

	@Test
	public void testParseAs() {
		assertThat(parseAs(int.class, "123"),
				Is.<Object> is(Integer.valueOf(123)));
		assertThat(parseAs(double.class, "123"),
				Is.<Object> is(Double.valueOf(123)));
	}

}
