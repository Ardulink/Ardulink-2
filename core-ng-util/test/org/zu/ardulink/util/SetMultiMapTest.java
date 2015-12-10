package org.zu.ardulink.util;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SetMultiMapTest {

	@Test
	public void canPut() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.asMap(), is(buildMap(1, Collections.singleton("foo"))));
	}

	@Test
	public void canPutTwice() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.put(1, "foo"), is(FALSE));
		assertThat(s.asMap(), is(buildMap(1, Collections.singleton("foo"))));
	}

	@Test
	public void canRemoveExistingValue() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.remove(1, "foo"), is(TRUE));
		assertThat(s.asMap(), is(Collections.<Integer, Set<String>> emptyMap()));
	}

	@Test
	public void canHandleRemovesOfNonExistingValues() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.remove(1, "bar"), is(FALSE));
		assertThat(s.asMap(), is(buildMap(1, Collections.singleton("foo"))));
	}

	@Test
	public void canHandleRemovesOfNonExistingKeys() {
		SetMultiMap<Integer, String> s = new SetMultiMap<Integer, String>();
		assertThat(s.put(1, "foo"), is(TRUE));
		assertThat(s.remove(2, "foo"), is(FALSE));
		assertThat(s.asMap(), is(buildMap(1, Collections.singleton("foo"))));
	}

	private static Map<Integer, Set<String>> buildMap(Integer key,
			Set<String> value) {
		Map<Integer, Set<String>> m = new HashMap<Integer, Set<String>>();
		m.put(key, value);
		return m;
	}

}
