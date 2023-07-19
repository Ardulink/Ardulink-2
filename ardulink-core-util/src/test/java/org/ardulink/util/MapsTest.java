package org.ardulink.util;

import static org.ardulink.util.Maps.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

class MapsTest {

	private static final class CollectingBiConsumer<K, V> implements BiConsumer<K, V> {

		List<Entry<K, V>> entries = new ArrayList<>();

		@Override
		public void accept(K k, V v) {
			entries.add(Maps.entry(k, v));
		}
	}

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
		CollectingBiConsumer<Integer, String> consumer = new CollectingBiConsumer<>();
		Maps.consumeIfPresent(map, 1, consumer);
		assertThat(consumer.entries).containsExactly(entry(1, "a"));
	}

	@Test
	void testConsumeIfPresentWithoutMatch() {
		CollectingBiConsumer<Integer, String> consumer = new CollectingBiConsumer<>();
		Maps.consumeIfPresent(map, 42, consumer);
		assertThat(consumer.entries).isEmpty();
	}

}
