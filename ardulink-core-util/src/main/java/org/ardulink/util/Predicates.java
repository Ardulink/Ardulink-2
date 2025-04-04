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

import static java.lang.String.format;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Predicates {

	private Predicates() {
		super();
	}

	public static <I, O> Predicate<I> attribute(Function<I, O> function, Predicate<O> predicate) {
		return new Predicate<I>() {

			@Override
			public boolean test(I in) {
				return predicate.test(function.apply(in));
			}

			public String toString() {
				return format("AttributePredicate: function: %s, predicate: %s", function, predicate);
			}

		};
	}

}
