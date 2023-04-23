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

import static java.lang.System.arraycopy;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Bytes {

	private Bytes() {
		super();
	}

	public static byte[] concat(byte[] bytes1, byte[] bytes2) {
		byte[] newArray = new byte[bytes1.length + bytes2.length];
		arraycopy(bytes1, 0, newArray, 0, bytes1.length);
		arraycopy(bytes2, 0, newArray, bytes1.length, bytes2.length);
		return newArray;
	}

	public static int indexOf(byte[] array, byte[] search) {
		return indexOf(array, search, 0);
	}

	public static int indexOf(byte[] array, byte[] search, int startpos) {
		return indexOf(array, search, startpos, array.length);
	}

	public static int indexOf(byte[] array, byte[] search, int startpos, int endpos) {
		outer: for (int i = startpos; i < endpos - search.length + 1; i++) {
			for (int j = 0; j < search.length; j++) {
				if (array[i + j] != search[j]) {
					continue outer;
				}
			}
			return i;
		}
		return -1;
	}

	public static boolean equals(byte[] bytes1, byte[] bytes2, int endPos) {
		if (bytes1.length < endPos || bytes2.length < endPos) {
			return false;
		}
		for (int i = 0; i < endPos; i++) {
			if (bytes1[i] != bytes2[i]) {
				return false;
			}
		}
		return true;
	}

}
