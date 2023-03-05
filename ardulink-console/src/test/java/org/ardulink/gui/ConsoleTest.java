package org.ardulink.gui;

import static java.awt.GraphicsEnvironment.isHeadless;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;

import org.ardulink.legacy.Link;
import org.junit.jupiter.api.Test;

class ConsoleTest {

	private final Link connectLink = mock(Link.class);

	@Test
	void whenStartedConnectIsEnabledAndDisconnnectIsDisabled() {
		assumeFalse(isHeadless());
		Console console = newConsole();
		assertThat(console.getLink(), is(nullValue()));
		assertThat(console.btnConnect.isEnabled(), is(TRUE));
		assertThat(console.btnDisconnect.isEnabled(), is(FALSE));
	}

	@Test
	void whenConnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		assumeFalse(isHeadless());
		Console console = newConsole();
		console.btnConnect.doClick();

		assertThat(console.getLink(), is(connectLink));
		assertThat(console.btnConnect.isEnabled(), is(FALSE));
		assertThat(console.btnDisconnect.isEnabled(), is(TRUE));
	}

	@Test
	void whenDisconnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		assumeFalse(isHeadless());
		Console console = newConsole();
		console.btnConnect.doClick();
		console.btnDisconnect.doClick();

		assertThat(console.getLink(), is(nullValue()));
		assertThat(console.btnConnect.isEnabled(), is(TRUE));
		assertThat(console.btnDisconnect.isEnabled(), is(FALSE));
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
