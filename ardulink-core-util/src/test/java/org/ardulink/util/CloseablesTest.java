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

import static org.ardulink.util.Closeables.closeQuietly;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.Closeable;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class CloseablesTest {

	@Test
	void closeQuietlyOk() {
		Closeable noop = () -> {
		};
		assertDoesNotThrow(() -> closeQuietly(noop));
	}

	@Test
	void closeQuietlyException() {
		IOException ioex = new IOException("close error");
		Closeable closeable = () -> {
			throw ioex;
		};
		assertThatRuntimeException().isThrownBy(() -> closeQuietly(closeable)).withCause(ioex);
	}

}
