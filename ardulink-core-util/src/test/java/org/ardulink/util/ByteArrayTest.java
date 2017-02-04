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

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;;


public class ByteArrayTest {

	@Test
	public void arrayAreEqualTest() {
		byte[] a1 = "abcde".getBytes();
		byte[] a2 = "abcde".getBytes();
		
		boolean retvalue = ByteArray.arraysAreEqual(a1, a2);
		assertTrue(retvalue);
	}
	
	@Test
	public void arrayContains() {
		
		ByteArray byteArray = new ByteArray("this is a byte array".getBytes());
		
		assertTrue(byteArray.contains("te ar".getBytes()));
		assertFalse(byteArray.contains("these".getBytes()));
		assertTrue(byteArray.contains("array".getBytes()));
		
	}

	@Test
	public void arrayNext() {
		
		ByteArray byteArray = new ByteArray("1111.:2222_3333:.4444".getBytes());
		
		byte[] token0 = byteArray.next("//".getBytes());
		byte[] token1 = byteArray.next(".:".getBytes());
		byte[] token2 = byteArray.next("_".getBytes());
		byte[] token3 = byteArray.next(":.".getBytes());
		byte[] token4 = byteArray.next(".:".getBytes());
		
		assertThat(token0, equalTo(null));
		assertThat(token1, equalTo("1111".getBytes()));
		assertThat(token2, equalTo("2222".getBytes()));
		assertThat(token3, equalTo("3333".getBytes()));
		assertThat(token4, equalTo(null));
		assertThat(byteArray.size(), equalTo(4));

		byte[] token5 = byteArray.next("4444".getBytes());
		assertThat(token5, equalTo(null));
		assertThat(byteArray.size(), equalTo(0));
	}
	
}
