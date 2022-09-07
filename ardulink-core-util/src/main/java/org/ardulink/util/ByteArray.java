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

import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.util.Arrays;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
/**
 * This class is <b>not</b> threadsafe.
 * 
 * @author Peter Fichtner
 */
public class ByteArray {

	private static final int MAX_BUFFER_LEN = 2048;

	private final byte[] byteArray;

	private int pointer;

	public ByteArray() {
		this(MAX_BUFFER_LEN);
	}

	public ByteArray(int maxLength) {
		byteArray = new byte[maxLength];
	}

	public boolean contains(byte[] delimiter) {
		return indexOf(delimiter) >= 0;
	}

	private int indexOf(byte[] delimiter) {
		checkState(checkNotNull(delimiter, "delimiter must not be null").length > 0, "delimiter must not be empty");
		return Bytes.indexOf(byteArray, delimiter, 0, pointer);
	}

	/**
	 * Appends the passed buffer to the internal byte[].
	 * 
	 * @param buffer    the data to append
	 */
	public void append(byte[] buffer) {
		append(buffer, buffer.length);
	}

	/**
	 * Appends the passed buffer to the internal byte[].
	 * 
	 * @param buffer    the data to append
	 * @param bytesRead length of the data to append from <code>buffer</code>
	 */
	public void append(byte[] buffer, int bytesRead) {
		checkArgument(this.pointer + bytesRead <= this.byteArray.length, "buffer overrun");
		System.arraycopy(buffer, 0, this.byteArray, this.pointer, bytesRead);
		this.pointer += bytesRead;
	}

	public void append(byte b) {
		append(new byte[] { b }, 1);
	}

	public int length() {
		return pointer;
	}

	public byte[] copy() {
		return Arrays.copyOf(byteArray, pointer);
	}

	public void clear() {
		pointer = 0;
	}

}
