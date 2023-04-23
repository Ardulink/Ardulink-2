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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class BytesTest {

	@Test
	void abcAndAbc() {
		byte[] abc = "abc".getBytes();
		areEqual(abc, abc, 0, true);
		areEqual(abc, abc, 1, true);
		areEqual(abc, abc, 2, true);
		areEqual(abc, abc, 3, true);
		areEqual(abc, abc, 4, false);
	}

	@Test
	void abAndAbc() {
		byte[] abc = "abc".getBytes();
		byte[] ab = "ab".getBytes();
		areEqual(abc, ab, 0, true);
		areEqual(abc, ab, 1, true);
		areEqual(abc, ab, 2, true);
		areEqual(abc, ab, 3, false);
		areEqual(abc, ab, 4, false);
	}

	@Test
	void abcAndEmpty() {
		byte[] abc = "abc".getBytes();
		byte[] ab = "".getBytes();
		areEqual(abc, ab, 0, true);
		areEqual(abc, ab, 1, false);
		areEqual(abc, ab, 2, false);
		areEqual(abc, ab, 3, false);
		areEqual(abc, ab, 4, false);
	}

	private void areEqual(byte[] bytes1, byte[] bytes2, int len, boolean expected) {
		assertThat(Bytes.equals(bytes1, bytes2, len)).isEqualTo(expected);
		assertThat(Bytes.equals(bytes2, bytes1, len)).isEqualTo(expected);
	}

}
