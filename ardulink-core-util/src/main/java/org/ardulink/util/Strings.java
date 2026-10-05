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

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Strings {

	private Strings() {
		super();
	}

	public static boolean nullOrEmpty(String string) {
		return string == null || string.isEmpty();
	}

	/**
	 * Checks whether the specified string starts with the given prefix, ignoring
	 * character case.
	 * <p>
	 * This method avoids creating a lowercased copy of the input string, as would
	 * occur with {@code name.toLowerCase().startsWith(...)}, and only compares the
	 * characters relevant to the prefix.
	 *
	 * @param name   the string to check
	 * @param prefix the prefix to look for
	 * @return {@code true} if {@code name} starts with {@code prefix}, ignoring
	 *         case; {@code false} otherwise
	 */
	public static boolean startsWithIgnoreCase(String name, String prefix) {
		int prefixLength = prefix.length();
		return name.length() >= prefixLength && name.regionMatches(true, 0, prefix, 0, prefixLength);
	}

	/**
	 * Checks whether the specified string ends with the given suffix, ignoring
	 * character case.
	 * <p>
	 * This method avoids creating a lowercased copy of the input string, as would
	 * occur with {@code name.toLowerCase().endsWith(...)}, and only compares the
	 * characters relevant to the suffix.
	 *
	 * @param name   the string to check
	 * @param suffix the suffix to look for
	 * @return {@code true} if {@code name} ends with {@code suffix}, ignoring case;
	 *         {@code false} otherwise
	 */
	public static boolean endsWithIgnoreCase(String name, String suffix) {
		int suffixLength = suffix.length();
		return name.length() >= suffixLength
				&& name.regionMatches(true, name.length() - suffixLength, suffix, 0, suffixLength);
	}

	public static String swapUpperLower(String string) {
		char[] charArray = string.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (isUpperCase(c)) {
				charArray[i] = toLowerCase(c);
			} else if (isLowerCase(c)) {
				charArray[i] = toUpperCase(c);
			}
		}
		return new String(charArray);
	}

}
