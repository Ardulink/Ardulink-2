package org.ardulink.util;

import static org.ardulink.util.Closeables.closeQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		assertThat(assertThrows(RuntimeException.class, () -> closeQuietly(closeable))).hasCause(ioex);
	}

}
