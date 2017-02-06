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

import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class ByteArray {
	
	private byte[] byteArray;
	private int lastFoundIndex;

	public ByteArray(byte[] byteArray) {
		checkNotNull(byteArray, "Array can't be null");
		this.byteArray = byteArray;
	}

	public ByteArray(ByteArrayOutputStream os) {
		this(os.toByteArray());
	}

	public boolean contains(byte[] delimiter) {
		
		checkNotNull(delimiter, "delimiter can't be null");
		checkState(delimiter.length > 0, "delimiter length can't be %s", delimiter.length);
		
		for (int i = 0; i < byteArray.length - delimiter.length + 1; i++) {
			if(byteArray[i] == delimiter[0]) {
				if(arraysAreEqual(Arrays.copyOfRange(byteArray, i, i + delimiter.length), delimiter)) {
					lastFoundIndex = i;
					return true;
				}
			}
		}
		
		return false;
	}

	public byte[] next(byte[] delimiter) {
		
		if(!contains(delimiter)) {
			return null;
		}
		
		byte[] retvalue = Arrays.copyOfRange(byteArray, 0, lastFoundIndex);
		byteArray = Arrays.copyOfRange(byteArray, lastFoundIndex + delimiter.length, byteArray.length);
		
		return retvalue;
	}

	/**
	 * Input arrays have to be not null and with the same length
	 * @param a1
	 * @param a2
	 * @return true if arrays are equal
	 */
	public static boolean arraysAreEqual(byte[] a1, byte[] a2) {

		checkNotNull(a1, "a1 can't be null");
		checkNotNull(a2, "a2 can't be null");
		checkState(a1.length == a2.length, "Arrays haven't the same length %s <> %s", a1.length, a2.length);

		for (int i = 0; i < a1.length; i++) {
			if(a1[i] != a2[i]) {
				return false;
			}
		}
		return true;
	}

	public int size() {
		return byteArray.length;
	}

	/**
	 * 
	 * @return the array (WARNING modifies to the returned array are shared with the ByteArray object)
	 */
	public byte[] getRemainingBytes() {
		return byteArray;
	}
}
