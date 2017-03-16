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

import java.util.Arrays;

public class ByteArray {

	private static final int MAX_BUFFER_LEN = 2048;

	private byte[] byteArray;

	private int pointer;

	public ByteArray(byte[] byteArray) {
		this.byteArray = checkNotNull(byteArray, "Array must not be null");
	}

	public boolean contains(byte[] delimiter) {
		return indexOf(delimiter) >= 0;
	}

	private int indexOf(byte[] delimiter) {
		checkState(
				checkNotNull(delimiter, "delimiter must not be null").length > 0,
				"delimiter must not be empty");
		return Bytes.indexOf(byteArray, delimiter, pointer);
	}

	public byte[] next(byte[] delimiter) {
		int nextPointer = indexOf(delimiter);
		if (nextPointer < 0) {
			return null;
		}

		byte[] next = Arrays.copyOfRange(this.byteArray, this.pointer,
				nextPointer);
		this.pointer = nextPointer + delimiter.length;
		return next;
	}

	/**
	 * Appends the passed buffer to the internal byte[]. Simultaneously the
	 * internal byte[] is compacted by the data already returned by calls to
	 * {@link #next(byte[])}.
	 * 
	 * @param buffer
	 *            the data to append
	 * @param bytesRead
	 *            length of the data to append from <code>buffer</code>
	 */
	public void append(byte[] buffer, int bytesRead) {
		int newLen = this.byteArray.length - pointer + bytesRead;
		checkState(newLen <= MAX_BUFFER_LEN,
				"Buffer size exceed MAX_BUFFER_LEN (%s > %s)", newLen,
				MAX_BUFFER_LEN);
		byte[] newBytes = new byte[newLen];
		System.arraycopy(this.byteArray, 0 + pointer, newBytes, 0,
				this.byteArray.length - pointer);
		System.arraycopy(buffer, 0, newBytes, this.byteArray.length - pointer,
				bytesRead);
		this.pointer = 0;
		this.byteArray = newBytes;
	}

	public int size() {
		return byteArray.length - pointer;
	}

}
