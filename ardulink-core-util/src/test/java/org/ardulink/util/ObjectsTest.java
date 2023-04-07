package org.ardulink.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ObjectsTest {

	@Test
	void equalObjectsResultInTrue() {
		assertThat(Objects.equals("something", "something")).isTrue();
	}

	@Test
	void nullAndNullAreEqual() {
		assertThat(Objects.equals(null, null)).isTrue();
	}

	@Test
	void twoItemsWhereOneOfThemIsNullResultInFalse() {
		assertThat(Objects.equals("something", null)).isFalse();
		assertThat(Objects.equals(null, "something")).isFalse();
	}

}
