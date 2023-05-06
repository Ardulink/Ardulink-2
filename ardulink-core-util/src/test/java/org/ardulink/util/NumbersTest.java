package org.ardulink.util;

import static org.ardulink.util.Numbers.convertTo;
import static org.ardulink.util.Numbers.numberType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

class NumbersTest {

	@Property
	void integers(@ForAll Integer value) {
		convert(value);
	}

	@Property
	void bytes(@ForAll Byte value) {
		convert(value);
	}

	@Property
	void shorts(@ForAll Short value) {
		convert(value);
	}

	@Property
	void longs(@ForAll Long value) {
		convert(value);
	}

	@Property
	void floats(@ForAll Float value) {
		convert(value);
	}

	@Property
	void doubles(@ForAll Double value) {
		convert(value);
	}

	private void convert(Number value) {
		assertThat(convertTo(value, Integer.class)).isEqualTo(value.intValue());
		assertThat(convertTo(value, Byte.class)).isEqualTo(value.byteValue());
		assertThat(convertTo(value, Short.class)).isEqualTo(value.shortValue());
		assertThat(convertTo(value, Long.class)).isEqualTo(value.longValue());
		assertThat(convertTo(value, Float.class)).isEqualTo(value.floatValue());
		assertThat(convertTo(value, Double.class)).isEqualTo(value.doubleValue());
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