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
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.util.Integers.constrain;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.ardulink.core.Link;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.core.events.FilteredEventListenerAdapter;
import org.ardulink.gui.facility.IntMinMaxModel;
import org.ardulink.util.Throwables;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * This class implements the AnalogReadChangeListener interface and is able to
 * listen events coming from arduino board about analog pin state change.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class AnalogPinStatus extends JPanel implements Linkable {

	private static final String TOGGLE_TEXT_ON = "On";
	private static final String TOGGLE_TEXT_OFF = "Off";

	private static final Font FONT_11 = new Font("SansSerif", Font.PLAIN, 11);
	private static final Font FONT_12 = new Font("SansSerif", Font.PLAIN, 12);

	private static final long serialVersionUID = 7927439571760351922L;

	private JLabel valueLabel;
	private JLabel voltValueLbl;
	private JProgressBar progressBar;
	private JComboBox<Integer> minValueComboBox;
	private IntMinMaxModel minValueComboBoxModel;
	private JComboBox<Integer> maxValueComboBox;
	private IntMinMaxModel maxValueComboBoxModel;
	private JComboBox<Integer> pinComboBox;
	private IntMinMaxModel pinComboBoxModel;
	private JLabel lblPowerPinController;
	private JToggleButton isActiveButton;

	private transient EventListener listener;
	private final DecimalFormat voltageFormat = new DecimalFormat("#.###");

	private FilteredEventListenerAdapter listener() {
		AnalogPin pin = analogPin(pinComboBoxModel.getSelectedItem().intValue());
		return new FilteredEventListenerAdapter(pin, new EventListenerAdapter() {

			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				Integer value = event.getValue();
				valueLabel.setText(String.valueOf(value));
				voltValueLbl.setText(voltageFormat.format(voltage(value)) + " V");
				progressBar.setValue(progress(value));
			}

			private float voltage(int value) {
				return (((float) value) * 5.0f) / 1023.0f;
			}

			private int progress(int value) {
				int minValue = getMinValue();
				int maxValue = getMaxValue();
				return maxValue == minValue //
						? 0 //
						: (int) (((value - minValue) * 100.0) / (maxValue - minValue));
			}
		});
	}

	private transient Link link;

	/**
	 * Create the panel.
	 */
	public AnalogPinStatus() {
		setPreferredSize(new Dimension(140, 260));
		setLayout(null);

		JLabel lblPowerPin = new JLabel("Power Pin:");
		lblPowerPin.setFont(FONT_11);
		lblPowerPin.setBounds(10, 40, 59, 14);
		add(lblPowerPin);

		// TODO define a method to be able to change the set of controllable pins. This
		// way you can work with different boards than an Arduino UNO
		// pinComboBox.setModel(new DefaultComboBoxModel(new String[] {"3", "5", "6",
		// "9", "10", "11"}));
		pinComboBoxModel = new IntMinMaxModel(0, 40);
		pinComboBox = new JComboBox<>(pinComboBoxModel);
		pinComboBox.setSelectedItem(Integer.valueOf(0));
		pinComboBox.setBounds(65, 36, 62, 22);
		add(pinComboBox);

		maxValueComboBoxModel = new IntMinMaxModel(0, 1023).withLastItemSelected();
		maxValueComboBox = new JComboBox<>(maxValueComboBoxModel);
		maxValueComboBox.setBounds(65, 65, 62, 22);
		add(maxValueComboBox);

		minValueComboBoxModel = new IntMinMaxModel(0, 1023).withFirstItemSelected();
		minValueComboBox = new JComboBox<>(minValueComboBoxModel);
		minValueComboBox.setBounds(65, 217, 62, 22);
		add(minValueComboBox);

		JLabel lblMaxValue = new JLabel("Max Value:");
		lblMaxValue.setFont(FONT_11);
		lblMaxValue.setBounds(10, 69, 59, 14);
		add(lblMaxValue);

		JLabel lblMinValue = new JLabel("Min Value:");
		lblMinValue.setFont(FONT_11);
		lblMinValue.setBounds(10, 221, 59, 14);
		add(lblMinValue);

		progressBar = new JProgressBar();
		progressBar.setFont(FONT_11);
		progressBar.setStringPainted(true);
		progressBar.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		progressBar.setOrientation(SwingConstants.VERTICAL);
		progressBar.setBounds(111, 99, 16, 108);
		add(progressBar);

		lblPowerPinController = new JLabel("Analog PIN Status");
		lblPowerPinController.setFont(FONT_12);
		lblPowerPinController.setToolTipText("");
		lblPowerPinController.setHorizontalAlignment(SwingConstants.CENTER);
		lblPowerPinController.setBounds(10, 11, 117, 14);
		add(lblPowerPinController);

		JLabel lblVoltOutput = new JLabel("Volt Output:");
		lblVoltOutput.setFont(FONT_11);
		lblVoltOutput.setBounds(10, 143, 59, 14);
		add(lblVoltOutput);

		voltValueLbl = new JLabel("0V");
		voltValueLbl.setFont(FONT_11);
		voltValueLbl.setBounds(10, 157, 76, 14);
		add(voltValueLbl);

		JLabel lblCurrentValue = new JLabel("Current Value:");
		lblCurrentValue.setFont(FONT_11);
		lblCurrentValue.setBounds(10, 98, 76, 14);
		add(lblCurrentValue);

		valueLabel = new JLabel();
		valueLabel.setBounds(10, 112, 55, 22);
		valueLabel.setText("0");
		add(valueLabel);

		isActiveButton = new JToggleButton(TOGGLE_TEXT_OFF);
		isActiveButton.addItemListener(e -> {
			if (e.getStateChange() == SELECTED) {
				isActiveButton.setText(TOGGLE_TEXT_ON);
				addListener();
			} else if (e.getStateChange() == DESELECTED) {
				isActiveButton.setText(TOGGLE_TEXT_OFF);
				removeListener();
			}

			updateComponentsEnabledState();
		});

		isActiveButton.setBounds(10, 177, 76, 28);
		add(isActiveButton);

		minValueComboBox.addActionListener(__ -> fixAndUpdate(minValueComboBoxModel, getMaxValue()));
		maxValueComboBox.addActionListener(__ -> fixAndUpdate(maxValueComboBoxModel, getMinValue()));
	}

	private void fixAndUpdate(IntMinMaxModel minMaxModel, int value) {
		fixWhenExceed(minMaxModel, value);
		updateValue();
	}

	private void fixWhenExceed(IntMinMaxModel minMaxModel, int value) {
		if (getMinValue() > getMaxValue()) {
			minMaxModel.setSelectedItem(value);
		}
	}

	/**
	 * Set the pin to control
	 * 
	 * @param pin the pin number to control
	 * @return
	 */
	public AnalogPinStatus setPin(int pin) {
		pinComboBoxModel.setSelectedItem(pin);
		return this;
	}

	@Override
	public void setLink(Link link) {
		removeListener();
		this.link = checkNotNull(link, "link must not be null");
	}

	private void addListener() {
		try {
			link.addListener(listener = listener());
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private void removeListener() {
		if (this.listener != null) {
			try {
				this.link.removeListener(this.listener);
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		}
	}

	public void setTitle(String title) {
		lblPowerPinController.setText(title);
	}

	public int getValue() {
		return Integer.parseInt(valueLabel.getText());
	}

	public void setValue(int value) {
		valueLabel.setText(String.valueOf(constrain(value, getMinValue(), getMaxValue())));
	}

	public int getMinValue() {
		return minValueComboBoxModel.getSelectedItem().intValue();
	}

	public int getMaxValue() {
		return maxValueComboBoxModel.getSelectedItem().intValue();
	}

	private void updateValue() {
		setValue(getValue());
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		updateComponentsEnabledState();
	}

	/**
	 * Updates the enabled state of the internal components based on:
	 * <ul>
	 * <li>The current enabled state of this panel ({@code isEnabled()})</li>
	 * <li>The current toggle state of the sensor button</li>
	 * </ul>
	 *
	 * This method ensures that the UI reflects the correct interactive state:
	 * <ul>
	 * <li>If the panel is disabled, all components (including the toggle) are
	 * disabled, regardless of toggle state</li>
	 * <li>If the toggle is "On", sensor input is active and only relevant UI
	 * remains enabled</li>
	 * <li>If the toggle is "Off", all input fields can be edited</li>
	 * </ul>
	 *
	 * Should be called whenever the toggle state changes or when
	 * {@code setEnabled(boolean)} is called.
	 */
	private void updateComponentsEnabledState() {
		boolean panelIsEnabled = isEnabled();
		boolean isOn = isActiveButton.isSelected();

		isActiveButton.setEnabled(panelIsEnabled);
		pinComboBox.setEnabled(panelIsEnabled && !isOn);
		minValueComboBox.setEnabled(panelIsEnabled && !isOn);
		maxValueComboBox.setEnabled(panelIsEnabled && !isOn);
		progressBar.setEnabled(panelIsEnabled && isOn);
	}

}
