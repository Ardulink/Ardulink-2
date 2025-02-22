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
import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;
import static org.ardulink.console.SwingPredicates.is;
import static org.ardulink.console.SwingPredicates.withName;
import static org.ardulink.console.SwingPredicates.withSelectedItem;
import static org.ardulink.console.SwingPredicates.withText;
import static org.ardulink.console.SwingSelector.findComponent;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;

import org.ardulink.core.Link;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.gui.PWMController;
import org.ardulink.gui.SwitchController;
import org.ardulink.gui.connectionpanel.ConnectionPanel;

public class ConsolePage {

	private final Console console;

	public ConsolePage(Console console) {
		this.console = console;
	}

	public Link getLink() {
		return console.getLink();
	}

	public JButton connectButton() {
		return findComponent(console, JButton.class, withText("Connect"));
	}

	public JButton disconnectButton() {
		return findComponent(console, JButton.class, withText("Disconnect"));
	}

	public void connect() {
		connectButton().doClick();
	}

	public void disconnect() {
		disconnectButton().doClick();
	}

	private ConnectionPanel connectionPanel() {
		return findComponent(console, ConnectionPanel.class);
	}

	public void useConnection(String connection) {
		ConnectionPanel connectionPanel = connectionPanel();
		JComboBox<?> comboBox = findComponent(connectionPanel, JComboBox.class);
		comboBox.setSelectedItem(connection);
		assert comboBox.getSelectedItem().equals(connection) : format("Failed to set %s, valid values are: %s",
				connection, Arrays.toString(range(0, comboBox.getItemCount()).mapToObj(comboBox::getItemAt).toArray()));
	}

	public <T> T attributeChooser(String name, Class<T> type) {
		JPanel attributePanel = findComponent(connectionPanel(), JPanel.class);
		JLabel label = findComponent(attributePanel, JLabel.class, l -> name.equals(l.getText()));
		List<Component> components = asList(attributePanel.getComponents());
		return type.cast(components.get(components.indexOf(label) + 1));
	}

	public JPanel switchPanel() {
		return tabWithTitle("Switch Panel");
	}

	public JPanel powerPanel() {
		return tabWithTitle("Power Panel");
	}

	private JPanel tabWithTitle(String title) {
		JTabbedPane tabbedPane = findComponent(console, JTabbedPane.class);
		return range(0, tabbedPane.getTabCount()) //
				.filter(i -> title.equals(tabbedPane.getTitleAt(i))) //
				.mapToObj(i -> tabbedPane.getComponentAt(i)) //
				.findFirst() //
				.map(JPanel.class::cast) //
				.orElseThrow(
						() -> new IllegalStateException(format("No tab with title '%s' found in %s", title, console)));
	}

	public JToggleButton digitalSwitch(DigitalPin pin) {
		return findComponent(pinController(pin), JToggleButton.class);
	}

	public SwitchController pinController(DigitalPin pin) {
		return findComponent(switchPanel(), SwitchController.class, s -> findComponent(s, JComboBox.class,
				withName("pinComboBox").and(is(JComboBox.class, withSelectedItem(pin.pinNum())))) != null);
	}

	public JSlider analogSlider(AnalogPin pin) {
		return findComponent(pwmController(pin), JSlider.class);
	}

	public PWMController pwmController(AnalogPin pin) {
		return findComponent(powerPanel(), PWMController.class, p -> findComponent(p, JComboBox.class,
				withName("pinComboBox").and(is(JComboBox.class, withSelectedItem(pin.pinNum())))) != null);
	}

}
