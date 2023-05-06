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

	INT(Integer.class, Number::intValue), //
	BYTE(Byte.class, Number::byteValue), //
	SHORT(Short.class, Number::shortValue), //
	LONG(Long.class, Number::longValue), //
	FLOAT(Float.class, Number::floatValue), //
	DOUBLE(Double.class, Number::doubleValue);

	private final Class<?> type;
	private final Function<Number, Number> converter;

	Numbers(Class<?> type, Function<Number, Number> converter) {
		this.type = type;
		this.converter = converter;
	}

	public Number convert(Number source) {
		return converter.apply(source);
	}

	public static <T extends Number> T convertTo(Number source, Class<T> target) {
		return target.cast(numberType(target).convert(source));
	}

	public static <T extends Number> Numbers numberType(Class<T> target) {
		return streamOfAll().filter(t -> t.type.equals(target)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unsupported type " + target));
	}

	private static Stream<Numbers> streamOfAll() {
		return EnumSet.allOf(Numbers.class).stream();
	}

}