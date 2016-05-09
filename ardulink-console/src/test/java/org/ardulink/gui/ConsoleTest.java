package org.ardulink.gui;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.ardulink.legacy.Link;
import org.junit.Test;

public class ConsoleTest {

	private final Link connectLink = mock(Link.class);

	private final Console console = new Console() {
		protected Link createLink() {
			return connectLink;
		};
	};

	@Test
	public void whenStartedConnectIsEnabledAndDisconnnectIsDisabled() {
		assertThat(console.getLink(), is(nullValue()));
		assertThat(console.btnConnect.isEnabled(), is(TRUE));
		assertThat(console.btnDisconnect.isEnabled(), is(FALSE));
	}

	@Test
	public void whenConnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		console.btnConnect.doClick();

		assertThat(console.getLink(), is(connectLink));
		assertThat(console.btnConnect.isEnabled(), is(FALSE));
		assertThat(console.btnDisconnect.isEnabled(), is(TRUE));
	}

	@Test
	public void whenDisconnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		console.btnConnect.doClick();
		console.btnDisconnect.doClick();

		assertThat(console.getLink(), is(nullValue()));
		assertThat(console.btnConnect.isEnabled(), is(TRUE));
		assertThat(console.btnDisconnect.isEnabled(), is(FALSE));
	}

}
