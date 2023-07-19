package org.ardulink.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.BiConsumer;

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
		BiConsumer<Integer, String> consumerMock = consumerMock();
		Maps.consumeIfPresent(map, 1, consumerMock);
		verify(consumerMock).accept(1, "a");
		verifyNoMoreInteractions(consumerMock);
	}

	@Test
	void testConsumeIfPresentWithoutMatch() {
		BiConsumer<Integer, String> consumerMock = consumerMock();
		Maps.consumeIfPresent(map, 42, consumerMock);
		verifyNoMoreInteractions(consumerMock);
	}

	@SuppressWarnings("unchecked")
	private BiConsumer<Integer, String> consumerMock() {
		return mock(BiConsumer.class);
	}

}
