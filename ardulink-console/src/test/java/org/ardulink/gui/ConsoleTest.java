package org.ardulink.gui;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.stream.IntStream.range;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;
import static org.ardulink.core.linkmanager.LinkManager.ARDULINK_SCHEME;
import static org.ardulink.core.virtual.console.VirtualConnectionLinkFactory.VIRTUAL_CONSOLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.approvaltests.awt.AwtApprovals;
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

	@Test
	@DisabledIf(IS_HEADLESS)
	void approvalsVerify() throws Exception {
		Console console = newConsole();
		String name = format("%s://%s", ARDULINK_SCHEME, VIRTUAL_CONSOLE_NAME);

		JComboBox<?> comboBox = findComboBoxContainingItem(console, name);
		comboBox.setSelectedItem(name);
		repaint(console);

		AwtApprovals.verify(console.getContentPane());
	}

	private void repaint(Console console) {
		console.repaint();
		await().until(console::isOpaque);
	}

	private static JComboBox<?> findComboBoxContainingItem(Container container, Object item) {
		return findComponentRecursively(container, JComboBox.class) //
				.filter(comboBox -> containsItem((JComboBox<?>) comboBox, item)) //
				.map(comboBox -> (JComboBox<?>) comboBox) //
				.findFirst() //
				.orElse(null) //
		;
	}

	private static <T extends Component> Stream<T> findComponentRecursively(Container container, Class<T> clazz) {
		return Arrays.stream(container.getComponents()).flatMap(comp -> {
			if (clazz.isInstance(comp)) {
				return Stream.of(clazz.cast(comp));
			} else if (comp instanceof Container) {
				return findComponentRecursively((Container) comp, clazz);
			} else {
				return Stream.empty();
			}
		});
	}

	private static boolean containsItem(JComboBox<?> comboBox, Object item) {
		return Arrays.stream(range(0, comboBox.getItemCount()) //
				.mapToObj(comboBox::getItemAt) //
				.toArray()) //
				.anyMatch(e -> Objects.equals(e, item)) //
		;
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
