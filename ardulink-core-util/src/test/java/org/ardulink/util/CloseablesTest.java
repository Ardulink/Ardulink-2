package org.ardulink.util;

import static org.ardulink.util.Closeables.closeQuietly;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.Closeable;
import java.io.IOException;

import org.junit.jupiter.api.Test;

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
