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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

	Closeable closeable = mock(Closeable.class);

	@Test
	void closeQuietlyOk() throws IOException {
		closeQuietly(closeable);
		verify(closeable).close();
	}

	@Test
	void closeQuietlyException() throws IOException {
		IOException ioex = new IOException("close error");
		doThrow(ioex).when(closeable).close();
		assertThatRuntimeException().isThrownBy(() -> closeQuietly(closeable)).withCause(ioex);
	}

}
