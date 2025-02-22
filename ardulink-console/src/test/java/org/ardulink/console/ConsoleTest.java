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
package org.ardulink.console;

import static java.lang.String.format;
import static org.ardulink.core.linkmanager.LinkManager.ARDULINK_SCHEME;
import static org.ardulink.core.virtual.console.VirtualConnectionLinkFactory.VIRTUAL_CONSOLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

import org.approvaltests.awt.AwtApprovals;
import org.ardulink.core.Link;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

/**
 * [ardulinktitle] [ardulinkversion] This is the ready ardulink console a
 * complete SWING application to manage an Arduino board. Console has several
 * tabs with all ready arduino components. Each tab is able to do a specific
 * action sending or listening for messages to arduino or from arduino board.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ConsoleTest {

	private static final String IS_HEADLESS = "java.awt.GraphicsEnvironment#isHeadless";

	private final Link connectLink = mock(Link.class);

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenStartedConnectIsEnabledAndDisconnnectIsDisabled() {
		ConsolePage page = new ConsolePage(newConsole());
		assertThat(page.getLink()).isNull();
		assertThat(page.connectButton().isEnabled()).isTrue();
		assertThat(page.disconnectButton().isEnabled()).isFalse();
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenConnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		ConsolePage page = new ConsolePage(newConsole());
		page.connect();

		assertThat(page.getLink()).isSameAs(connectLink);
		assertThat(page.connectButton().isEnabled()).isFalse();
		assertThat(page.disconnectButton().isEnabled()).isTrue();
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenDisconnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		ConsolePage page = new ConsolePage(newConsole());
		page.connect();
		page.disconnect();

		assertThat(page.getLink()).isNull();
		assertThat(page.connectButton().isEnabled()).isTrue();
		assertThat(page.disconnectButton().isEnabled()).isFalse();
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void approvalsVerify() {
		Console console = newConsole();
		ConsolePage page = new ConsolePage(console);
		page.useConnection(format("%s://%s", ARDULINK_SCHEME, VIRTUAL_CONSOLE_NAME));
		repaint(console);

		AwtApprovals.verify(console.getContentPane());
	}

	private void repaint(Console console) {
		await().until(() -> console.isOpaque());
	}

	@SuppressWarnings("serial")
	private Console newConsole() {
		return new Console() {
			@Override
			protected Link createLink() {
				return connectLink;
			}
		};
	}

}
