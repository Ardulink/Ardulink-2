package org.ardulink.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

public class IteratorsTest {

	@Test
	public void getFirst() {
		assertThat(Iterators.getFirst(iteratorOf(1)).get(), is(1));
		assertThat(Iterators.getFirst(iteratorOf(1, 2)).get(), is(1));
	}

	@Test
	public void getLast() {
		assertThat(Iterators.getLast(iteratorOf(1)).get(), is(1));
		assertThat(Iterators.getLast(iteratorOf(1, 2)).get(), is(2));
	}

	private <T> Iterator<T> iteratorOf(T... elements) {
		return Arrays.asList(elements).iterator();
	}

}
