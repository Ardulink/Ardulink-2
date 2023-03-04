package org.ardulink.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class ObjectsTest {

	@Test
	void equalObjectsResultInTrue() {
		assertThat(Objects.equals("something", "something"), is(true));
	}

	@Test
	void nullAndNullAreEqual() {
		assertThat(Objects.equals(null, null), is(true));
	}

	@Test
	void twoItemsWhereOneOfThemIsNullResultInFalse() {
		assertThat(Objects.equals("something", null), is(false));
		assertThat(Objects.equals(null, "something"), is(false));
	}

}
