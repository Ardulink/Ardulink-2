package org.ardulink.console.fx;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;

import org.ardulink.core.Link;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import javafx.application.Platform;

class ConsoleWindowTest {

	private static final String IS_HEADLESS = "java.awt.GraphicsEnvironment#isHeadless";

	private final Link mockLink = mock(Link.class);

	@BeforeAll
	static void startJavaFx() throws Exception {
		Platform.setImplicitExit(false);
		CompletableFuture<Void> ready = new CompletableFuture<>();
		try {
			Platform.startup(() -> ready.complete(null));
		} catch (IllegalStateException e) {
			ready.complete(null);
		}
		ready.get(10, SECONDS);
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenStartedConnectIsEnabledAndDisconnectIsDisabled() throws Exception {
		try (ConsolePage page = newPage()) {
			assertThat(page.isConnectEnabled()).isTrue();
			assertThat(page.isDisconnectEnabled()).isFalse();
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenConnectButtonClickedButtonsToggle() throws Exception {
		try (ConsolePage page = newPage()) {
			page.connect();
			assertThat(page.isConnectEnabled()).isFalse();
			assertThat(page.isDisconnectEnabled()).isTrue();
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenDisconnectButtonClickedButtonsToggle() throws Exception {
		try (ConsolePage page = newPage()) {
			page.connect();
			page.disconnect();
			assertThat(page.isConnectEnabled()).isTrue();
			assertThat(page.isDisconnectEnabled()).isFalse();
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenWindowClosedLinkIsClosed() throws Exception {
		ConsolePage page = newPage();
		page.connect();
		page.close();
		verify(mockLink, timeout(SECONDS.toMillis(2))).close();
	}

	private ConsolePage newPage() throws Exception {
		return new ConsolePage(() -> mockLink);
	}
}
