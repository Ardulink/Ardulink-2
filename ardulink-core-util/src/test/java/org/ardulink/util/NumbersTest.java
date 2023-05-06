package org.ardulink.util;

import static org.ardulink.util.Numbers.convertTo;
import static org.ardulink.util.Numbers.numberType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class NumbersTest {

	@Test
	void canConvert() {
		assertThat(convertTo(1L, Double.class)).isEqualTo(Long.valueOf(1).doubleValue());
		assertThat(convertTo(1.0, Double.class)).isEqualTo(Double.valueOf(1).doubleValue());
		assertThat(convertTo(1.0, Long.class)).isEqualTo(Double.valueOf(1.0).longValue());
		assertThat(convertTo(Long.MAX_VALUE, Long.class)).isEqualTo(Long.valueOf(Long.MAX_VALUE).longValue());
	}

	@Test
	void noRoundingWhenConvertingFromFloatingPointNumbers() {
		assertThat(convertTo(1.1, Long.class)).isEqualTo(Double.valueOf(1.1).longValue());
		assertThat(convertTo(1.9999, Long.class)).isEqualTo(Double.valueOf(1.9999).longValue());
		assertThat(convertTo(2.0, Long.class)).isEqualTo(Double.valueOf(2.0).longValue());
	}

	@Test
	void overflow() {
		assertThat(convertTo(Long.MAX_VALUE, Integer.class)).isEqualTo(Long.valueOf(Long.MAX_VALUE).intValue());
	}

	@ParameterizedTest
	@EnumSource(Numbers.class)
	void min(Numbers numberType) {
		Map<Object, Object> minValues = MapBuilder.newMapBuilder().put(Integer.class, Integer.MIN_VALUE)
				.put(Byte.class, Byte.MIN_VALUE).put(Short.class, Short.MIN_VALUE).put(Long.class, Long.MIN_VALUE)
				.put(Float.class, Float.MIN_VALUE).put(Double.class, Double.MIN_VALUE).build();
		assertThat(numberType(numberType.getType()).min()).isEqualTo(minValues.get(numberType.getType()));
	}

	@ParameterizedTest
	@EnumSource(Numbers.class)
	void max(Numbers numberType) {
		Map<Object, Object> maxValues = MapBuilder.newMapBuilder().put(Integer.class, Integer.MAX_VALUE)
				.put(Byte.class, Byte.MAX_VALUE).put(Short.class, Short.MAX_VALUE).put(Long.class, Long.MAX_VALUE)
				.put(Float.class, Float.MAX_VALUE).put(Double.class, Double.MAX_VALUE).build();
		assertThat(numberType(numberType.getType()).max()).isEqualTo(maxValues.get(numberType.getType()));
	}

}
