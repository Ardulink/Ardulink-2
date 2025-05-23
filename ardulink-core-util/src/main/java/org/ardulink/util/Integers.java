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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Collection;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Integers {

	private Integers() {
		super();
	}

	public static int average(Collection<Integer> values) {
		return sum(values) / values.size();
	}

	public static int sum(Collection<Integer> values) {
		return values.stream().mapToInt(Integer::intValue).sum();
	}

	public static int constrain(int value, int min, int max) {
		return max(min(value, max), min);
	}

}
