package org.ardulink.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ObjectsTest {

	@Test
	public void equalObjectsResultInTrue() {
		assertThat(Objects.equals("something", "something"), is(true));
	}

	@Test
	public void nullAndNullAreEqual() {
		assertThat(Objects.equals(null, null), is(true));
	}

	@Test
	public void twoItemsWhereOneOfThemIsNullResultInFalse() {
		assertThat(Objects.equals("something", null), is(false));
		assertThat(Objects.equals(null, "something"), is(false));
	}

}
