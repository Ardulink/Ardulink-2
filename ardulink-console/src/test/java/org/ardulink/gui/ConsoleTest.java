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
package org.ardulink.gui;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.ardulink.core.linkmanager.LinkManager.ARDULINK_SCHEME;
import static org.ardulink.core.virtual.console.VirtualConnectionLinkFactory.VIRTUAL_CONSOLE_NAME;
import static org.ardulink.gui.SwingSelector.containsItem;
import static org.ardulink.gui.SwingSelector.findComponent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

import javax.swing.JComboBox;

import org.approvaltests.awt.AwtApprovals;
import org.ardulink.legacy.Link;
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

	@Test
	@DisabledIf(IS_HEADLESS)
	void approvalsVerify() throws Exception {
		Console console = newConsole();
		String name = format("%s://%s", ARDULINK_SCHEME, VIRTUAL_CONSOLE_NAME);
		findComponent(console, JComboBox.class, c -> containsItem(c, name)).setSelectedItem(name);
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
