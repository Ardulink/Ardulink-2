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

import java.util.EnumSet;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public enum Numbers {

	INTEGER(Integer.class, Integer.MIN_VALUE, Integer.MAX_VALUE, Number::intValue), //
	BYTE(Byte.class, Byte.MIN_VALUE, Byte.MAX_VALUE, Number::byteValue), //
	SHORT(Short.class, Short.MIN_VALUE, Short.MAX_VALUE, Number::shortValue), //
	LONG(Long.class, Long.MIN_VALUE, Long.MAX_VALUE, Number::longValue), //
	FLOAT(Float.class, Float.MIN_VALUE, Float.MAX_VALUE, Number::floatValue), //
	DOUBLE(Double.class, Double.MIN_VALUE, Double.MAX_VALUE, Number::doubleValue);

	private final Class<Number> type;
	private final Number min;
	private final Number max;
	private final Function<Number, Number> converter;

	@SuppressWarnings("unchecked")
	Numbers(Class<?> type, Number min, Number max, Function<Number, Number> converter) {
		this.type = (Class<Number>) type;
		this.min = min;
		this.max = max;
		this.converter = converter;
	}

	public Class<Number> getType() {
		return type;
	}

	public Number min() {
		return min;
	}

	public Number max() {
		return max;
	}

	public Number convert(Number source) {
		return converter.apply(source);
	}

	public static <T extends Number> T convertTo(Number source, Class<T> target) {
		return target.cast(numberType(target).convert(source));
	}

	public static <T extends Number> Numbers numberType(Class<T> target) {
		return streamOfAll().filter(t -> t.getType().equals(target)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unsupported type " + target));
	}

	private static Stream<Numbers> streamOfAll() {
		return EnumSet.allOf(Numbers.class).stream();
	}

}