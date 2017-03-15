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
	private int pointer;

	public ByteArray(byte[] byteArray) {
		resetWith(byteArray);
	}

	public ByteArray(ByteArrayOutputStream os) {
		this(os.toByteArray());
	}

	public void resetWith(byte[] byteArray) {
		this.byteArray = checkNotNull(byteArray, "Array must not be null");
		this.pointer = 0;
	}

	public boolean contains(byte[] delimiter) {
		return posOf(delimiter) >= 0;
	}

	private int posOf(byte[] delimiter) {
		checkNotNull(delimiter, "delimiter must not be null");
		checkState(delimiter.length > 0, "delimiter must not be empty");

		for (int pos = pointer; pos < byteArray.length - delimiter.length + 1; pos++) {
			if (isAtPos(delimiter, pos)) {
				return pos;
			}
		}
		return -1;
	}

	private boolean isAtPos(byte[] delimiter, int pos) {
		for (int i = 0; i < delimiter.length; i++) {
			if (byteArray[pos + i] != delimiter[i]) {
				return false;
			}

		}
		return true;
	}

	public byte[] next(byte[] delimiter) {
		int nextPointer = posOf(delimiter);
		if (nextPointer < 0) {
			return null;
		}

		byte[] next = Arrays.copyOfRange(byteArray, pointer, nextPointer);
		this.pointer = nextPointer + delimiter.length;
		return next;
	}

	public int size() {
		return byteArray.length - pointer;
	}

	/**
	 * 
	 * @return the array (WARNING modifies to the returned array are shared with
	 *         the ByteArray object)
	 */
	public byte[] getRemainingBytes() {
		return byteArray;
	}

}
