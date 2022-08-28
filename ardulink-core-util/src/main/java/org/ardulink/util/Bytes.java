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
import static org.ardulink.util.anno.LapsedWith.JDK8;

import java.util.List;

import org.ardulink.util.anno.LapsedWith;

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

	@LapsedWith(module = JDK8, value = "Streams")
	public static byte[] hexStringToBytes(String hex) {
		byte[] data = new byte[hex.length() / 2];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) ((Character.digit(hex.charAt(i * 2 + 0), 16) << 4)
					+ Character.digit(hex.charAt(i * 2 + 1), 16));
		}
		return data;
	}

	@LapsedWith(module = JDK8, value = "Streams")
	public static List<String> bytesToHex(byte[] bytes) {
		List<String> hexValues = Lists.newArrayList();
		for (int i = 0; i < bytes.length; i++) {
			hexValues.add(String.format("%02X", bytes[i]));
		}
		return hexValues;
	}

}
