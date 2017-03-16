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

	public static int indexOf(byte[] array, byte[] target) {
		return indexOf(array, target, 0);
	}

	public static int indexOf(byte[] array, byte[] target, int startpos) {
		outer: for (int i = startpos; i < array.length - target.length + 1; i++) {
			for (int j = 0; j < target.length; j++) {
				if (array[i + j] != target[j]) {
					continue outer;
				}
			}
			return i;
		}
		return -1;
	}

}
