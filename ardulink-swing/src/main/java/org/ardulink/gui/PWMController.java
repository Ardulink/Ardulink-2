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

import static java.awt.ComponentOrientation.RIGHT_TO_LEFT;
import static java.awt.Font.PLAIN;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.VERTICAL;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.gui.util.LinkReplacer.doReplace;
import static org.ardulink.util.Integers.constrain;

import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;

import org.ardulink.core.Link;
import org.ardulink.gui.event.PWMChangeEvent;
import org.ardulink.gui.event.PWMControllerListener;
import org.ardulink.gui.facility.IntMinMaxModel;
import org.ardulink.util.Throwables;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * This class can manage power with modulation arduino pins sending specific
 * messages to the arduino board. It has many components to ensure maximum
 * flexibility in the management of these pins.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class PWMController extends JPanel implements Linkable {

	private static final long serialVersionUID = 7927439571760351922L;

	private static final Font FONT_11 = new Font("SansSerif", PLAIN, 11);
	private static final Font FONT_12 = new Font("SansSerif", PLAIN, 12);

	private static final float VOLTAGE_CONVERSION = 5F / 255F;

	private JSlider powerSlider;
	private JComboBox<Integer> valueComboBox;
	private IntMinMaxModel valueComboBoxModel;
	private JLabel voltValueLbl;
	private JCheckBox chckbxContChange;
	private JProgressBar progressBar;
	private IntMinMaxModel maxValueComboBoxModel;
	private IntMinMaxModel minValueComboBoxModel;
	private JComboBox<Integer> pinComboBox;
	private JLabel lblPowerPinController;

	private transient List<PWMControllerListener> pwmControllerListeners = new CopyOnWriteArrayList<>();
	private final DecimalFormat voltageFormat = new DecimalFormat("#.### V");

	private transient Link link;

	/**
	 * Create the panel.
	 */
	public PWMController() {
		setPreferredSize(new Dimension(195, 260));
		setLayout(null);

		powerSlider = new JSlider();
		powerSlider.setFont(FONT_11);
		powerSlider.setMajorTickSpacing(15);
		powerSlider.setPaintLabels(true);
		powerSlider.setPaintTicks(true);
		powerSlider.setMaximum(255);
		powerSlider.setValue(0);
		powerSlider.setOrientation(VERTICAL);
		powerSlider.setBounds(126, 38, 59, 199);
		add(powerSlider);

		JLabel lblPowerPin = new JLabel("Power Pin:");
		lblPowerPin.setFont(FONT_11);
		lblPowerPin.setBounds(10, 40, 59, 14);
		add(lblPowerPin);

		// TODO define a method to be able to change the set of controllable pins. This
		// way you can work with different boards than an Arduino UNO
		// pinComboBox.setModel(new DefaultComboBoxModel(new Integer[] {3, 5, 6, 9, 10,
		// 11 }));
		IntMinMaxModel pinComboBoxModel = new IntMinMaxModel(0, 40);
		pinComboBox = new JComboBox<Integer>(pinComboBoxModel);
		pinComboBox.setName("pinComboBox");
		pinComboBox.setSelectedItem(Integer.valueOf(11));
		pinComboBox.setBounds(65, 36, 55, 22);
		add(pinComboBox);

		maxValueComboBoxModel = new IntMinMaxModel(0, 1023).withSelectedItem(255);
		JComboBox<Integer> maxValueComboBox = new JComboBox<>(maxValueComboBoxModel);
		maxValueComboBox.setBounds(65, 65, 55, 22);
		add(maxValueComboBox);

		minValueComboBoxModel = new IntMinMaxModel(0, 1023).withFirstItemSelected();
		JComboBox<Integer> minValueComboBox = new JComboBox<>(minValueComboBoxModel);
		minValueComboBox.setBounds(65, 217, 55, 22);
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
		progressBar.setOrientation(VERTICAL);
		progressBar.setBounds(96, 98, 16, 108);
		add(progressBar);

		lblPowerPinController = new JLabel("Power Pin Controller");
		lblPowerPinController.setFont(FONT_12);
		lblPowerPinController.setToolTipText("Power With Modulation");
		lblPowerPinController.setHorizontalAlignment(CENTER);
		lblPowerPinController.setBounds(10, 11, 175, 14);
		add(lblPowerPinController);

		JLabel lblVoltOutput = new JLabel("Volt Output:");
		lblVoltOutput.setFont(FONT_11);
		lblVoltOutput.setBounds(10, 143, 59, 14);
		add(lblVoltOutput);

		voltValueLbl = new JLabel();
		voltValueLbl.setFont(FONT_11);
		voltValueLbl.setBounds(10, 157, 76, 14);
		add(voltValueLbl);

		JLabel lblCurrentValue = new JLabel("Current Value:");
		lblCurrentValue.setFont(FONT_11);
		lblCurrentValue.setBounds(10, 98, 76, 14);
		add(lblCurrentValue);

		valueComboBoxModel = new IntMinMaxModel(0, 255).withFirstItemSelected();
		valueComboBox = new JComboBox<>(valueComboBoxModel);
		valueComboBox.addActionListener(__ -> {
			int comboBoxCurrentValue = valueComboBoxModel.getSelectedItem().intValue();
			int powerSliderCurrentValue = powerSlider.getValue();
			if (comboBoxCurrentValue != powerSliderCurrentValue) {
				powerSlider.setValue(comboBoxCurrentValue);
			}
		});
		valueComboBox.setBounds(10, 112, 55, 22);
		add(valueComboBox);

		JLabel lblContinuousChange = new JLabel("Cont. Change:");
		lblContinuousChange.setFont(FONT_11);
		lblContinuousChange.setToolTipText("Continuous Change");
		lblContinuousChange.setBounds(10, 176, 73, 14);
		add(lblContinuousChange);

		chckbxContChange = new JCheckBox("");
		chckbxContChange.setRequestFocusEnabled(false);
		chckbxContChange.setRolloverEnabled(true);
		chckbxContChange.setComponentOrientation(RIGHT_TO_LEFT);
		chckbxContChange.setSelected(true);
		chckbxContChange.setBounds(6, 188, 21, 22);
		add(chckbxContChange);

		powerSlider.addChangeListener(__ -> {
			if (!powerSlider.getValueIsAdjusting() || chckbxContChange.isSelected()) {
				int power = powerSlider.getValue();
				try {
					link.switchAnalogPin(analogPin(getPin()), power);
					setPower(power);
					notifyListeners(new PWMChangeEvent(power));
				} catch (IOException e) {
					throw Throwables.propagate(e);
				}
			}
		});

		minValueComboBox.addActionListener(__ -> {
			update(minValueComboBoxModel, maxValue());
			powerSlider.setMinimum(minValue());
		});
		maxValueComboBox.addActionListener(__ -> {
			update(maxValueComboBoxModel, minValue());
			powerSlider.setMaximum(maxValue());
		});
		setPower(0);
	}

	private int getPin() {
		return (int) pinComboBox.getSelectedItem();
	}

	private void update(IntMinMaxModel model, int value) {
		int minValue = minValue();
		int maxValue = maxValue();
		if (minValue > maxValue) {
			model.setSelectedItem(value);
		}

		valueComboBoxModel = new IntMinMaxModel(minValue, maxValue);
		valueComboBox.setModel(valueComboBoxModel);
		powerSlider.setMajorTickSpacing((maxValue - minValue) / 18);
	}

	private int minValue() {
		return minValueComboBoxModel.getSelectedItem().intValue();
	}

	private int maxValue() {
		return maxValueComboBoxModel.getSelectedItem().intValue();
	}

	private void setPower(int value) {
		valueComboBoxModel.setSelectedItem(value);
		voltValueLbl.setText(voltageFormat.format(voltage(value)));
		progressBar.setValue(percent(value));
	}

	private static float voltage(int power) {
		return power * VOLTAGE_CONVERSION;
	}

	private int percent(int value) {
		int minValue = minValue();
		int maxValue = maxValue();
		return maxValue == minValue //
				? 0 //
				: (int) (((value - minValue) * 100D) / (maxValue - minValue));
	}

	/**
	 * Set the pin to control.
	 * 
	 * @param pin pin number
	 * @return
	 */
	public PWMController setPin(int pin) {
		pinComboBox.setSelectedItem(Integer.valueOf(pin));
		return this;
	}

	@Override
	public void setLink(Link link) {
		this.link = doReplace(this.link).with(link);
	}

	public void setTitle(String title) {
		lblPowerPinController.setText(title);
	}

	public boolean addPWMControllerListener(PWMControllerListener listener) {
		return pwmControllerListeners.add(listener);
	}

	private void notifyListeners(PWMChangeEvent event) {
		pwmControllerListeners.forEach(l -> l.pwmChanged(event));
	}

	public int getValue() {
		return valueComboBoxModel.getSelectedItem().intValue();
	}

	public void setValue(int value) {
		valueComboBoxModel.setSelectedItem(constrain(value, minValue(), maxValue()));
	}

}
