/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.util;

import static org.ardulink.util.Numbers.convertTo;
import static org.ardulink.util.Numbers.numberType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
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
		assertSoftly(s -> {
			s.assertThat(convertTo(value, Integer.class)).isEqualTo(value.intValue());
			s.assertThat(convertTo(value, Byte.class)).isEqualTo(value.byteValue());
			s.assertThat(convertTo(value, Short.class)).isEqualTo(value.shortValue());
			s.assertThat(convertTo(value, Long.class)).isEqualTo(value.longValue());
			s.assertThat(convertTo(value, Float.class)).isEqualTo(value.floatValue());
			s.assertThat(convertTo(value, Double.class)).isEqualTo(value.doubleValue());
		});
	}

	@ParameterizedTest
	@EnumSource(Numbers.class)
	void min(Numbers numberType) {
		Class<Number> type = numberType.getType();
		assertThat(numberType(type).min()).isEqualTo(minValue(type));
	}

	@ParameterizedTest
	@EnumSource(Numbers.class)
	void max(Numbers numberType) {
		Class<Number> type = numberType.getType();
		assertThat(numberType(type).max()).isEqualTo(maxValue(type));
	}

	static Object minValue(Class<Number> type) {
		return MapBuilder.newMapBuilder() //
				.put(Integer.class, Integer.MIN_VALUE) //
				.put(Byte.class, Byte.MIN_VALUE) //
				.put(Short.class, Short.MIN_VALUE) //
				.put(Long.class, Long.MIN_VALUE) //
				.put(Float.class, Float.MIN_VALUE) //
				.put(Double.class, Double.MIN_VALUE) //
				.build().get(type);
	}

	static Object maxValue(Class<Number> type) {
		return MapBuilder.newMapBuilder() //
				.put(Integer.class, Integer.MAX_VALUE) //
				.put(Byte.class, Byte.MAX_VALUE) //
				.put(Short.class, Short.MAX_VALUE) //
				.put(Long.class, Long.MAX_VALUE) //
				.put(Float.class, Float.MAX_VALUE) //
				.put(Double.class, Double.MAX_VALUE) //
				.build().get(type);
	}

}
