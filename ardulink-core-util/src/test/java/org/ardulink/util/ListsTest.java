package org.ardulink.util;

import static java.util.Arrays.asList;
import static org.ardulink.util.Lists.rangeCheckedGet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class ListsTest {

	@Test
	void testNewArrayListIterableOfT() {
		verifyIsMutable(Lists.newArrayList(asList("a", "b", "c")));
	}

	@Test
	void testNewArrayListIteratorOfT() {
		verifyIsMutable(Lists.newArrayList(asList("a", "b", "c").iterator()));
	}

	@Test
	void testNewArrayListTArray() {
		verifyIsMutable(Lists.newArrayList("a", "b", "c"));
	}

	private static void verifyIsMutable(List<String> list) {
		list.add("d");
		list.remove("d");
		list.clear();
	}

	@Test
	void testRangeCheckedGet() {
		List<String> listWithSize3 = asList("a", "b", "c");
		assertThat(Lists.rangeCheckedGet(listWithSize3, 2)).isEqualTo("c");
		assertThat(assertThrows(RuntimeException.class, () -> rangeCheckedGet(listWithSize3, 3)))
				.hasMessage("index out of range 0 >= 3 < 3");
		assertThat(assertThrows(RuntimeException.class, () -> rangeCheckedGet(listWithSize3, 3, "theAttributeName")))
				.hasMessage("theAttributeName out of range 0 >= 3 < 3");
	}

}
