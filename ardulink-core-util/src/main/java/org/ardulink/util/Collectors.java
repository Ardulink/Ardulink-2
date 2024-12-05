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

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.ardulink.util.anno.LapsedWith.JDK10;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Collectors {

	private Collectors() {
		super();
	}

	@LapsedWith(module = JDK10, value = "Collectors#toUnmodifiableMap")
	public static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
		return collectingAndThen(toList(), Collections::unmodifiableList);
	}

	@LapsedWith(module = JDK10, value = "Collectors#toUnmodifiableMap")
	public static <T, K, U> Collector<T, ?, Map<K, U>> toUnmodifiableMap(Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper) {
		return collectingAndThen(toMap(keyMapper, valueMapper), Collections::unmodifiableMap);
	}

	@LapsedWith(module = JDK10, value = "Collectors#toUnmodifiableMap")
	public static <T, K, U> Collector<T, ?, Map<K, U>> toUnmodifiableMap(Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction) {
		return collectingAndThen(toMap(keyMapper, valueMapper, mergeFunction), Collections::unmodifiableMap);
	}

}
