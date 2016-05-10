package org.ardulink.gui;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.mock;

import java.awt.GraphicsEnvironment;

import org.ardulink.legacy.Link;
import org.junit.Test;

public class ConsoleTest {

	private final Link connectLink = mock(Link.class);

	@Test
	public void whenStartedConnectIsEnabledAndDisconnnectIsDisabled() {
		assumeThat(GraphicsEnvironment.isHeadless(), is(FALSE));
		Console console = newConsole();
		assertThat(console.getLink(), is(nullValue()));
		assertThat(console.btnConnect.isEnabled(), is(TRUE));
		assertThat(console.btnDisconnect.isEnabled(), is(FALSE));
	}

	@Test
	public void whenConnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		assumeThat(GraphicsEnvironment.isHeadless(), is(FALSE));
		Console console = newConsole();
		console.btnConnect.doClick();

		assertThat(console.getLink(), is(connectLink));
		assertThat(console.btnConnect.isEnabled(), is(FALSE));
		assertThat(console.btnDisconnect.isEnabled(), is(TRUE));
	}

	@Test
	public void whenDisconnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		assumeThat(GraphicsEnvironment.isHeadless(), is(FALSE));
		Console console = newConsole();
		console.btnConnect.doClick();
		console.btnDisconnect.doClick();

		assertThat(console.getLink(), is(nullValue()));
		assertThat(console.btnConnect.isEnabled(), is(TRUE));
		assertThat(console.btnDisconnect.isEnabled(), is(FALSE));
	}

	private Console newConsole() {
		return new Console() {
			protected Link createLink() {
				return connectLink;
			};
		};
	}

}
