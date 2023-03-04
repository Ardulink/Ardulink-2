package org.ardulink.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

class IteratorsTest {

	@Test
	void getFirst() {
		assertThat(Iterators.getFirst(iteratorOf(1)).get(), is(1));
		assertThat(Iterators.getFirst(iteratorOf(1, 2)).get(), is(1));
	}

	@Test
	void getLast() {
		assertThat(Iterators.getLast(iteratorOf(1)).get(), is(1));
		assertThat(Iterators.getLast(iteratorOf(1, 2)).get(), is(2));
	}

	private <T> Iterator<T> iteratorOf(T... elements) {
		return Arrays.asList(elements).iterator();
	}

}
