package org.ardulink.util;

import static org.ardulink.util.Numbers.convertTo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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

}
