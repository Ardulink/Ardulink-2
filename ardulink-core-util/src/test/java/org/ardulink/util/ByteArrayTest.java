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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ByteArrayTest {

	private final ByteArray sut = new ByteArray(
			"1111.:2222_3333:.4444".getBytes());

	@Test
	public void arrayNext() {
		byte[] token;

		token = sut.next("//".getBytes());
		assertThat(token, is(nullValue()));
		assertThat(sut.size(), is(21));

		token = sut.next(".:".getBytes());
		assertThat(token, is("1111".getBytes()));
		assertThat(sut.size(), is(15));

		token = sut.next("_".getBytes());
		assertThat(token, is("2222".getBytes()));
		assertThat(sut.size(), is(10));

		token = sut.next(":.".getBytes());
		assertThat(token, is("3333".getBytes()));
		assertThat(sut.size(), is(4));

		token = sut.next(".:".getBytes());
		assertThat(token, is(nullValue()));
		assertThat(sut.size(), is(4));

		token = sut.next("4444".getBytes());
		assertThat(token, is(new byte[0]));
		assertThat(sut.size(), is(0));

		token = sut.next(" ".getBytes());
		assertThat(token, is(nullValue()));
		assertThat(sut.size(), is(0));
	}

}
