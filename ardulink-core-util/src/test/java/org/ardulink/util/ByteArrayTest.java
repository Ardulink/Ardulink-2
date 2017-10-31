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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ByteArrayTest {

	private final ByteArray sut = new ByteArray(10);

	@Test
	public void canAppend() {
		byte[] delimiter = ";".getBytes();
		append("abc;def;gh");
		assertNextIs(delimiter, "abc");
		assertNextIs(delimiter, "def");
		append("i;jkl;");
		assertNextIs(delimiter, "ghi");
		assertNextIs(delimiter, "jkl");
	}

	void assertNextIs(byte[] delimiter, String expected) {
		assertThat(new String(sut.next(delimiter)), is(expected));
	}

	private void append(String toAppend) {
		sut.append(toAppend.getBytes(), toAppend.length());
	}

}
