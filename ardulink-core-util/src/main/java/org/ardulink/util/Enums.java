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

import static java.util.Arrays.stream;
import static java.util.function.Predicate.isEqual;
import static org.ardulink.util.Predicates.attribute;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Enums {

	private Enums() {
		super();
	}

	public static <T extends Enum<?>> Optional<T> enumWithName(Class<T> targetType, String value) {
		return findEnumWithName(targetType, value::equals);
	}

	public static <T extends Enum<?>> Optional<T> enumWithNameIgnoreCase(Class<T> targetType, String value) {
		return findEnumWithName(targetType, value::equalsIgnoreCase);
	}

	private static <T extends Enum<?>> Optional<T> findEnumWithName(Class<T> targetType, Predicate<String> equal) {
		return stream(targetType.getEnumConstants()).filter(attribute(Enum::name, equal)).findFirst();
	}

}
