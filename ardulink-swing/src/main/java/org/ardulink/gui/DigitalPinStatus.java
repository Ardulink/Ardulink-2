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

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.FilteredEventListenerAdapter.filter;
import static org.ardulink.gui.Icons.icon;
import static org.ardulink.gui.util.LinkReplacer.doReplace;

import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.ardulink.core.Link;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.EventListenerAdapter;
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

	private static final long serialVersionUID = -7773514191770737230L;

	private static final String SENSOR_ON = "Sensor on";
	private static final String SENSOR_OFF = "Sensor off";

	private static final String HIGH_TEXT = "High";
	private static final String LOW_TEXT = "Low";

	private static final ImageIcon HIGH_ICON = icon("icons/blue-on-32.png");
	private static final ImageIcon LOW_ICON = icon("icons/blue-off-32.png");

	private final JLabel lblStatelabel;
	private final JComboBox<Integer> pinComboBox;
	private final IntMinMaxModel pinComboBoxModel;
	private final JLabel lblPin;
	private final JToggleButton isActiveButton;

	private transient Link link;

	private JPanel comboPanel;

	private transient EventListener listener;

	private EventListenerAdapter listener() {
		DigitalPin pin = digitalPin(pinComboBoxModel.getSelectedItem().intValue());
		return filter(pin, new EventListenerAdapter() {
			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				setValue(event.getValue().booleanValue());
			}
		});
	}

	/**
	 * Create the panel.
	 */
	public DigitalPinStatus() {
		setLayout(new GridLayout(3, 1, 0, 0));

		lblStatelabel = new JLabel();
		lblStatelabel.setHorizontalAlignment(SwingConstants.CENTER);
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
			if (e.getStateChange() == SELECTED) {
				startListening();
			} else if (e.getStateChange() == DESELECTED) {
				stopListening();
			}
			updateComponentsEnabledState();
		});
		add(isActiveButton);
		setValue(false);
	}

	private void setValue(boolean value) {
		lblStatelabel.setText(value ? HIGH_TEXT : LOW_TEXT);
		lblStatelabel.setIcon(value ? HIGH_ICON : LOW_ICON);
	}

	private void startListening() {
		try {
			link.addListener(listener = listener());
			isActiveButton.setText(SENSOR_ON);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private void stopListening() {
		if (listener != null) {
			try {
				link.removeListener(listener);
				listener = null;
				isActiveButton.setText(SENSOR_OFF);
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		}
	}

	public DigitalPinStatus setPin(int pin) {
		pinComboBoxModel.setSelectedItem(pin);
		return this;
	}

	@Override
	public void setLink(Link link) {
		stopListening();
		this.link = doReplace(this.link).with(link);
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
	 * <li>If the sensor is on, the pin combo is disabled</li>
	 * <li>If the sensor is off, the pin combo is enabled</li>
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
