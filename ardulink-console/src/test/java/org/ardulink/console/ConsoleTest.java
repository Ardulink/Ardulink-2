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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.console.SwingSelector.findComponent;
import static org.ardulink.core.NullLink.isNullLink;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.linkmanager.LinkManager.ARDULINK_SCHEME;
import static org.ardulink.core.virtual.console.VirtualConnectionLinkFactory.VIRTUAL_CONSOLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

import org.approvaltests.awt.AwtApprovals;
import org.ardulink.core.Link;
import org.ardulink.core.events.EventListener;
import org.ardulink.util.Throwables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

/**
 * /** [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ConsoleTest {

	private static final String IS_HEADLESS = "java.awt.GraphicsEnvironment#isHeadless";

	private final List<EventListener> eventListeners = new ArrayList<>();
	private final Link connectLink = createMock();

	private Link createMock() {
		try {
			Link link = mock(Link.class);
			doAnswer(invocation -> {
				eventListeners.add((EventListener) invocation.getArgument(0));
				return link;
			}).when(link).addListener(any(EventListener.class));
			return link;
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenStartedConnectIsEnabledAndDisconnnectIsDisabled() {
		try (ConsolePage page = new ConsolePage(newConsole())) {
			assertThat(isNullLink(page.getLink())).isTrue();
			assertThat(page.connectButton().isEnabled()).isTrue();
			assertThat(page.disconnectButton().isEnabled()).isFalse();
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenConnectButtonIsClickedLinkIsExchangedAndPopertyChangeEventsIsFired() {
		try (ConsolePage page = new ConsolePage(newConsole())) {
			page.connect();

			assertThat(page.getLink()).isSameAs(connectLink);
			assertThat(page.connectButton().isEnabled()).isFalse();
			assertThat(page.disconnectButton().isEnabled()).isTrue();
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void whenDisconnectButtonIsClickedLinkIsExchangedAndConnectDisconnetButtonsAreToggled() {
		try (ConsolePage page = new ConsolePage(newConsole())) {
			page.connect();
			page.disconnect();

			assertThat(isNullLink(page.getLink())).isTrue();
			assertThat(page.connectButton().isEnabled()).isTrue();
			assertThat(page.disconnectButton().isEnabled()).isFalse();
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void doesResetComponents() {
		String connection = "ardulink://virtual-random";

		try (ConsolePage page = new ConsolePage(new Console())) {
			page.useConnection(connection);

			JSlider pin11 = page.analogSlider(analogPin(11));
			JToggleButton pin12 = page.digitalSwitch(digitalPin(12));
			int initialPin11Value = pin11.getValue();
			boolean initialPin12Value = pin12.isSelected();

			page.connect();
			pin11.setValue(42);
			pin12.doClick();
			assert pin11.getValue() != initialPin11Value;
			assert pin12.isSelected() != initialPin12Value;

			page.disconnect();
			assertSoftly(s -> {
				s.assertThat(pin11.getValue()).isEqualTo(initialPin11Value);
				s.assertThat(pin12.isSelected()).isEqualTo(initialPin12Value);
			});
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void noInteractionAfterReconnectWhenRestoringFromStateStore() throws IOException {
		try (ConsolePage page = new ConsolePage(newConsole())) {
			JSlider pin11 = page.analogSlider(analogPin(11));
			JToggleButton pin12 = page.digitalSwitch(digitalPin(12));

			page.connect();
			pin11.setValue(42);
			pin12.doClick();

			verify(connectLink).switchAnalogPin(analogPin(11), 42);
			verify(connectLink).switchDigitalPin(digitalPin(12), true);
			verifyNoMoreInteractions(connectLink);

			// disconnect restores the states, e.g. sets pin 11 and 12 to 0 (but this must
			// not be done on the old (previous) link
			reset(connectLink);
			page.disconnect();

			verify(connectLink).close();
			verifyNoMoreInteractions(connectLink);
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void doesCloseConnectionWhenConsoleGetsClosed() throws IOException {
		try (ConsolePage page = new ConsolePage(newConsole())) {
			page.connect();
		}
		verify(connectLink, timeout(SECONDS.toMillis(1))).close();
		verifyNoMoreInteractions(connectLink);
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void approvalsVerify() {
		Console console = newConsole();
		try (ConsolePage page = new ConsolePage(console)) {
			page.useConnection(format("%s://%s", ARDULINK_SCHEME, VIRTUAL_CONSOLE_NAME));
			repaint(console);
			AwtApprovals.verify(console.getContentPane());
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void approvalsJustToEnsureThatTheSliderWasChanged() {
		Console console = newConsole();
		try (ConsolePage page = new ConsolePage(console)) {
			analogSensor0WithValue241(page);

			repaint(console);
			AwtApprovals.verify(console.getContentPane());
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void approvalsComponentsAreDisabledAfterDisconnect() {
		// when restoring states AFTER the connectionListener get called in Console, the
		// components previously activated still are enabled.
		Console console = newConsole();
		try (ConsolePage page = new ConsolePage(console)) {
			analogSensor0WithValue241(page);
			page.disconnect();
			repaint(console);
			AwtApprovals.verify(console.getContentPane());
		}
	}

	private void analogSensor0WithValue241(ConsolePage page) {
		page.useConnection(format("%s://%s", ARDULINK_SCHEME, VIRTUAL_CONSOLE_NAME));

		page.connect();
		JPanel analogSensorPanel = page.analogSensorPanel();
		page.showTab(analogSensorPanel);

		findComponent(analogSensorPanel, JToggleButton.class).doClick();
		eventListeners.forEach(l -> l.stateChanged(analogPinValueChanged(analogPin(0), 241)));
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
