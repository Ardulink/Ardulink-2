package org.ardulink.gui;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.ardulink.legacy.Link;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

class ConsoleTest {

	private static final String IS_HEADLESS = "java.awt.GraphicsEnvironment#isHeadless";

	private final Link connectLink = mock(Link.class);

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenStartedConnectIsEnabledAndDisconnnectIsDisabled() {
		Console console = newConsole();
		assertThat(console.getLink()).isNull();
		assertThat(console.btnConnect.isEnabled()).isEqualTo(TRUE);
		assertThat(console.btnDisconnect.isEnabled()).isEqualTo(FALSE);
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenConnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		Console console = newConsole();
		console.btnConnect.doClick();

		assertThat(console.getLink()).isEqualTo(connectLink);
		assertThat(console.btnConnect.isEnabled()).isEqualTo(FALSE);
		assertThat(console.btnDisconnect.isEnabled()).isEqualTo(TRUE);
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenDisconnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		Console console = newConsole();
		console.btnConnect.doClick();
		console.btnDisconnect.doClick();

		assertThat(console.getLink()).isNull();
		assertThat(console.btnConnect.isEnabled()).isEqualTo(TRUE);
		assertThat(console.btnDisconnect.isEnabled()).isEqualTo(FALSE);
	}

	private Console newConsole() {
		return new Console() {
			@Override
			protected Link createLink() {
				return connectLink;
			}
		};
	}

}
