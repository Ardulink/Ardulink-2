package org.ardulink.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ClassesTest {

	public static class TestClass1 {
	}

	public static class TestClass2 {
		public TestClass2(String someStringValue, int someIntValue) {
		}
	}

	@Test
	void constructor() {
		assertThat(Classes.constructor(TestClass1.class))
				.hasValueSatisfying(c -> assertThat(c.getParameterTypes()).isEmpty());
		assertThat(Classes.constructor(TestClass2.class, String.class, int.class))
				.hasValueSatisfying(c -> assertThat(c.getParameterTypes()).containsExactly(String.class, int.class));
		assertThat(Classes.constructor(TestClass2.class)).isEmpty();
	}

}
