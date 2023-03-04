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

import static org.ardulink.util.anno.LapsedWith.JDK11;

import java.util.function.Predicate;

import org.ardulink.util.anno.LapsedWith;

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

	@LapsedWith(module = JDK11, value = "Predicate#not")
	public static <T> Predicate<T> not(Predicate<T> predicate) {
		return predicate.negate();
	}

}
