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

import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.gui.Icons.icon;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.ardulink.core.Link;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.core.events.FilteredEventListenerAdapter;
import org.ardulink.gui.facility.IntMinMaxModel;
import org.ardulink.util.Throwables;

/**
 * [ardulinktitle] [ardulinkversion] This class implements the
 * DigitalReadChangeListener interface and is able to listen events coming from
 * arduino board about digital pin state change.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class DigitalPinStatus extends JPanel implements Linkable {

	private static final String SENSOR_ON = "Sensor on";
	private static final String SENSOR_OFF = "Sensor off";

	private static final long serialVersionUID = -7773514191770737230L;

	private final JLabel lblStatelabel;
	private final JComboBox<Integer> pinComboBox;
	private final IntMinMaxModel pinComboBoxModel;
	private final JLabel lblPin;
	private final JToggleButton isActiveButton;

	private transient Link link;

	private static final String HIGH = "High";
	private static final String LOW = "Low";

	private static final String HIGH_ICON_NAME = "icons/blue-on-32.png";
	private static final String LOW_ICON_NAME = "icons/blue-off-32.png";

	private static final ImageIcon HIGH_ICON = icon(HIGH_ICON_NAME);
	private static final ImageIcon LOW_ICON = icon(LOW_ICON_NAME);
	private JPanel comboPanel;

	private transient EventListener listener;

	private FilteredEventListenerAdapter listener() {
		return new FilteredEventListenerAdapter(digitalPin(pinComboBoxModel.getSelectedItem().intValue()),
				new EventListenerAdapter() {
					@Override
					public void stateChanged(DigitalPinValueChangedEvent event) {
						boolean value = event.getValue().booleanValue();
						lblStatelabel.setText(value ? HIGH : LOW);
						lblStatelabel.setIcon(value ? HIGH_ICON : LOW_ICON);
					}
				});
	}

	/**
	 * Create the panel.
	 */
	public DigitalPinStatus() {
		setLayout(new GridLayout(3, 1, 0, 0));

		lblStatelabel = new JLabel(LOW);
		lblStatelabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatelabel.setIcon(LOW_ICON);
		lblStatelabel.setEnabled(false);
		add(lblStatelabel);

		comboPanel = new JPanel();
		add(comboPanel);

		lblPin = new JLabel("Pin:");
		comboPanel.add(lblPin);

		pinComboBoxModel = new IntMinMaxModel(2, 40);
		pinComboBox = new JComboBox<>(pinComboBoxModel);
		comboPanel.add(pinComboBox);

		isActiveButton = new JToggleButton(SENSOR_OFF);
		isActiveButton.addItemListener(e -> {
			try {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					isActiveButton.setText(SENSOR_ON);
					addListener();
					updateComponentsEnabledState();
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					isActiveButton.setText(SENSOR_OFF);
					removeListener();
					updateComponentsEnabledState();
				}
			} catch (IOException ex) {
				throw Throwables.propagate(ex);
			}
		});
		add(isActiveButton);
	}

	public DigitalPinStatus setPin(int pin) {
		pinComboBoxModel.setSelectedItem(pin);
		return this;
	}

	@Override
	public void setLink(Link link) {
		removeListener();
		this.link = checkNotNull(link, "link must not be null");
	}

	private void addListener() throws IOException {
		link.addListener(listener = listener());
	}

	private void removeListener() {
		if (listener != null) {
			try {
				this.link.removeListener(listener);
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		updateComponentsEnabledState();
	}

	/**
	 * Updates internal component enabled states based on:
	 * <ul>
	 * <li>The panel's enabled state ({@link #isEnabled()})</li>
	 * <li>The toggle button sensor state (on or off)</li>
	 * </ul>
	 * 
	 * Ensures that UI components reflect expected interaction logic:
	 * <ul>
	 * <li>If the panel is disabled, all components are disabled</li>
	 * <li>If the sensor is on, the pin combo is disabled and status label is
	 * enabled</li>
	 * <li>If the sensor is off, the pin combo is enabled and status label is
	 * disabled</li>
	 * </ul>
	 */
	private void updateComponentsEnabledState() {
		boolean panelIsEnabled = isEnabled();
		boolean isOn = isActiveButton.isSelected();

		isActiveButton.setEnabled(panelIsEnabled);
		pinComboBox.setEnabled(panelIsEnabled && !isOn);
		lblStatelabel.setEnabled(panelIsEnabled && isOn);
	}

}
