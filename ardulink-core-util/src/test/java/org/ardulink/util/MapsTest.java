package org.ardulink.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class MapsTest {

	Map<Integer, String> map = MapBuilder.<Integer, String>newMapBuilder().put(1, "a").put(2, "b").put(3, "c").build();

	@Test
	void testToProperties() {
		Properties properties = Maps.toProperties(map);
		assertThat(properties).hasSameSizeAs(map).containsAllEntriesOf(map);
	}

	@Test
	void testEntry() {
		Entry<Integer, String> entry = Maps.entry(1, "a");
		Entry<Integer, String> otherEntry = Maps.entry(1, "a");
		assertThat(entry).hasSameHashCodeAs(otherEntry);
		assertThat(entry).isEqualTo(otherEntry);
	}

	@Test
	void testConsumeIfPresentWithMatch() {
		assertThat(Maps.getOptional(map, 1)).hasValue("a");
	}

	@Test
	void testConsumeIfPresentWithoutMatch() {
		assertThat(Maps.getOptional(map, 42)).isEmpty();
	}

}
