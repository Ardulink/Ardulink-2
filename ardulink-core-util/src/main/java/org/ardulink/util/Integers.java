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

import static org.ardulink.util.anno.LapsedWith.JDK8;

import java.util.Collection;

import org.ardulink.util.anno.LapsedWith;

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

	public static Optional<Integer> tryParse(String string) {
		try {
			return Optional.of(Integer.valueOf(string));
		} catch (NumberFormatException e) {
			return Optional.absent();
		}
	}
	
	@LapsedWith(value = JDK8, module = "IntStream")
	public static int average(Collection<Integer> values) {
		return sum(values) / values.size();
	}

	@LapsedWith(value = JDK8, module = "IntStream")
	public static int sum(Iterable<Integer> values) {
		int sum = 0;
		for (Integer integer : values) {
			sum += integer;
		}
		return sum;
	}

	@LapsedWith(module = JDK8, value = "Integer#compare")
	public static int compare(int x, int y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

}
