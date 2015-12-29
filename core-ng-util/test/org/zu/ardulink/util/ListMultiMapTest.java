package org.zu.ardulink.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.Test;

public class ListMultiMapTest {

	@Test
	public void iteratorOnEmpty() {
		ListMultiMap<Integer, String> sut = new ListMultiMap<Integer, String>();
		Iterator<Entry<Integer, String>> iterator = sut.iterator();
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	public void iteratorOnSingleElement() {
		ListMultiMap<Integer, String> sut = new ListMultiMap<Integer, String>();
		sut.put(1, "foo");
		Iterator<Entry<Integer, String>> iterator = sut.iterator();
		assertThat(iterator.hasNext(), is(true));
		Entry<Integer, String> next = iterator.next();
		assertThat(next.getKey(), is(1));
		assertThat(next.getValue(), is("foo"));
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	public void iteratorOnCollisionElement() {
		ListMultiMap<Integer, String> sut = new ListMultiMap<Integer, String>();
		sut.put(1, "foo");
		sut.put(1, "bar");
		Iterator<Entry<Integer, String>> iterator = sut.iterator();
		assertThat(iterator.hasNext(), is(true));
		Entry<Integer, String> next = iterator.next();
		assertThat(next.getKey(), is(1));
		assertThat(next.getValue(), is("foo"));
		next = iterator.next();
		assertThat(next.getKey(), is(1));
		assertThat(next.getValue(), is("bar"));
		assertThat(iterator.hasNext(), is(false));
	}

}
