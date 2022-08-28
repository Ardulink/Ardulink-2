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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class BytesTest {

	@Test
	public void testIndexOf() {
		byte[] oneToFive = new byte[] { 1, 2, 3, 4, 5 };
		assertThat(Bytes.indexOf(oneToFive, new byte[] { 1, 2 }), is(0));
		assertThat(Bytes.indexOf(oneToFive, new byte[] { 3, 4, 5 }), is(2));
		assertThat(Bytes.indexOf(oneToFive, new byte[] { 1, 3 }), is(-1));
	}

	@Test
	public void testBytesToHexString() {
		assertThat(Bytes.bytesToHex(new byte[] { 0, 1, 2, (byte) 253, (byte) 254, (byte) 255 }),
				is(Arrays.asList("00", "01", "02", "FD", "FE", "FF")));
	}

	@Test
	public void hexStringToBytes() {
		assertThat(Bytes.hexStringToBytes("00" + "01" + "02" + "FD" + "FE" + "FF"),
				is(new byte[] { 0, 1, 2, (byte) 253, (byte) 254, (byte) 255 }));
	}

}
