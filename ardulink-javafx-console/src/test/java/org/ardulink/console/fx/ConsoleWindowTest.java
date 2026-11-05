package org.ardulink.console.fx;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.approvaltests.Approvals;
import org.approvaltests.awt.AwtApprovals;
import org.approvaltests.core.Options;
import org.ardulink.core.Link;
import org.ardulink.core.events.EventListener;
import org.ardulink.util.Throwables;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import javafx.application.Platform;

class ConsoleWindowTest {

	private static final String IS_HEADLESS = "isHeadless";

	private final List<EventListener> eventListeners = new ArrayList<>();
	private final Link mockLink = createMockLink();

	private static boolean isHeadless() {
		return java.awt.GraphicsEnvironment.isHeadless();
	}

	private Link createMockLink() {
		try {
			Link link = mock(Link.class);
			doAnswer(i -> {
				eventListeners.add(i.getArgument(0));
				return link;
			}).when(link).addListener(any(EventListener.class));
			return link;
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@BeforeAll
	static void startJavaFx() throws Exception {
		Assumptions.assumeFalse(isHeadless());
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
	void approvalsVerify() throws Exception {
		try (ConsolePage page = newPage()) {
			AwtApprovals.verify(page.snapshot(), osSpecific());
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void approvalsVerifyConnected() throws Exception {
		try (ConsolePage page = newPage()) {
			page.connect();
			AwtApprovals.verify(page.snapshot(), osSpecific());
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void approvalsVerifyDisconnected() throws Exception {
		try (ConsolePage page = newPage()) {
			page.connect();
			page.disconnect();
			AwtApprovals.verify(page.snapshot(), osSpecific());
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void approvalsJustToEnsureThatTheSliderWasChanged() throws Exception {
		try (ConsolePage page = newPage()) {
			page.connect();
			page.selectTab("Analog Sensor Panel");
			page.clickFirstAnalogSensorToggle();
			eventListeners.forEach(l -> l.stateChanged(analogPinValueChanged(analogPin(0), 241)));
			page.waitForFxEvents();
			AwtApprovals.verify(page.snapshot(), osSpecific());
		}
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	void highShouldStayHighEvenAfterStopping() throws Exception {
		try (ConsolePage page = newPage()) {
			page.connect();
			page.selectTab("Digital Sensor Panel");
			page.clickFirstDigitalSensorToggle();
			eventListeners.forEach(l -> l.stateChanged(digitalPinValueChanged(digitalPin(2), true)));
			page.waitForFxEvents();
			page.clickFirstDigitalSensorToggle(); // stop listening
			page.waitForFxEvents();
			AwtApprovals.verify(page.snapshot(), osSpecific());
		}
	}

	private static Options osSpecific() {
		return new Options().and(Approvals.NAMES::asOsSpecificTest);
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
