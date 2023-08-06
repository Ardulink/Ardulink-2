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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ReadersTest {

	String lineSeparator = System.getProperty("line.separator");

	Reader empty() {
		return new StringReader("");
	}

	Reader fooBar() {
		return new StringReader("foo\nbar");
	}

	@Test
	void testLines() throws IOException {
		assertThat(Readers.lines(empty())).isEmpty();
		assertThat(Readers.lines(fooBar())).containsExactly("foo", "bar");
	}

	@Test
	void testToString() throws IOException {
		assertThat(Readers.toString(empty())).isEmpty();
		assertThat(Readers.toString(fooBar())).isEqualTo("foo" + lineSeparator + "bar");
	}

}
