/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/

package org.zu.ardulink.gui;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.zu.ardulink.gui.facility.IntMinMaxModel;
import org.zu.ardulink.legacy.Link;

import com.github.pfichtner.ardulink.core.events.AnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.EventListener;
import com.github.pfichtner.ardulink.core.events.EventListenerAdapter;
import com.github.pfichtner.ardulink.core.events.FilteredEventListenerAdapter;

/**
 * [ardulinktitle] [ardulinkversion]
 * This class implements the AnalogReadChangeListener interface and is able to listen
 * events coming from arduino board about analog pin state change.
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * @see AnalogReadChangeListener
 * 
 * [adsense]
 *
 */
public class AnalogPinStatus extends JPanel implements Linkable {

	private static final long serialVersionUID = 7927439571760351922L;

	private JLabel valueLabel;
	private JLabel voltValueLbl;
	private JProgressBar progressBar;
	private JComboBox minValueComboBox;
	private IntMinMaxModel minValueComboBoxModel;
	private JComboBox maxValueComboBox;
	private IntMinMaxModel maxValueComboBoxModel;
	private JComboBox pinComboBox;
	private IntMinMaxModel pinComboBoxModel;
	private JLabel lblPowerPinController;
	private JToggleButton tglbtnSensor;
	
	private EventListener listener;

	private FilteredEventListenerAdapter listener() {
		return new FilteredEventListenerAdapter(
				analogPin(pinComboBoxModel.getSelectedItem().intValue()),
				new EventListenerAdapter() {
					@Override
					public void stateChanged(
							AnalogPinValueChangedEvent event) {
						Integer value = event.getValue();
						valueLabel.setText(Integer.toString(value));

						float volt = ((float) (((float) value) * 5.0f)) / 1023.0f;
						voltValueLbl.setText(String.valueOf(volt) + "V");

						float progress = ((float) (((float) (value - getMinValue())) * 100.0f))
								/ ((float) getMaxValue() - (float) getMinValue());
						progressBar.setValue((int) progress);
					}
				});
	}

	private Link link = Link.getDefaultInstance();

	/**
	 * Create the panel.
	 */
	public AnalogPinStatus() {
		setPreferredSize(new Dimension(140, 260));
		setLayout(null);
		
		JLabel lblPowerPin = new JLabel("Power Pin:");
		lblPowerPin.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblPowerPin.setBounds(10, 40, 59, 14);
		add(lblPowerPin);
		
		// TODO definire un metodo per poter cambiare l'insieme dei pin controllabili. In questo modo si può lavorare anche con schede diverse da Arduino UNO
		// pinComboBox.setModel(new DefaultComboBoxModel(new String[] {"3", "5", "6", "9", "10", "11"}));
		pinComboBoxModel = new IntMinMaxModel(0, 40);
		pinComboBox = new JComboBox(pinComboBoxModel);
		pinComboBox.setSelectedItem(Integer.valueOf(0));
		pinComboBox.setBounds(65, 36, 62, 22);
		add(pinComboBox);
		
		maxValueComboBoxModel = new IntMinMaxModel(0, 1023).withLastItemSelected();
		maxValueComboBox = new JComboBox(maxValueComboBoxModel);
		maxValueComboBox.setBounds(65, 65, 62, 22);
		add(maxValueComboBox);

		minValueComboBoxModel = new IntMinMaxModel(0, 1023).withFirstItemSelected();
		minValueComboBox = new JComboBox(minValueComboBoxModel);
		minValueComboBox.setBounds(65, 217, 62, 22);
		add(minValueComboBox);
		
		JLabel lblMaxValue = new JLabel("Max Value:");
		lblMaxValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblMaxValue.setBounds(10, 69, 59, 14);
		add(lblMaxValue);
		
		JLabel lblMinValue = new JLabel("Min Value:");
		lblMinValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblMinValue.setBounds(10, 221, 59, 14);
		add(lblMinValue);
		
		progressBar = new JProgressBar();
		progressBar.setFont(new Font("SansSerif", Font.PLAIN, 11));
		progressBar.setStringPainted(true);
		progressBar.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		progressBar.setOrientation(SwingConstants.VERTICAL);
		progressBar.setBounds(111, 99, 16, 108);
		add(progressBar);
		
		lblPowerPinController = new JLabel("Analog PIN Status");
		lblPowerPinController.setFont(new Font("SansSerif", Font.PLAIN, 12));
		lblPowerPinController.setToolTipText("");
		lblPowerPinController.setHorizontalAlignment(SwingConstants.CENTER);
		lblPowerPinController.setBounds(10, 11, 117, 14);
		add(lblPowerPinController);
		
		JLabel lblVoltOutput = new JLabel("Volt Output:");
		lblVoltOutput.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblVoltOutput.setBounds(10, 143, 59, 14);
		add(lblVoltOutput);
		
		voltValueLbl = new JLabel("0V");
		voltValueLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
		voltValueLbl.setBounds(10, 157, 76, 14);
		add(voltValueLbl);
		
		JLabel lblCurrentValue = new JLabel("Current Value:");
		lblCurrentValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblCurrentValue.setBounds(10, 98, 76, 14);
		add(lblCurrentValue);
		
		valueLabel = new JLabel();
		valueLabel.setBounds(10, 112, 55, 22);
		valueLabel.setText("0");
		add(valueLabel);
		
		tglbtnSensor = new JToggleButton("Off");
		tglbtnSensor.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					link.addAnalogReadChangeListener((listener = listener()));
					
					tglbtnSensor.setText("On");
					pinComboBox.setEnabled(false);
					minValueComboBox.setEnabled(false);
					maxValueComboBox.setEnabled(false);
					
					progressBar.setEnabled(true);
					
					
				} else if(e.getStateChange() == ItemEvent.DESELECTED) {
					link.removeAnalogReadChangeListener(listener);
					
					tglbtnSensor.setText("Off");
					pinComboBox.setEnabled(true);
					minValueComboBox.setEnabled(true);
					maxValueComboBox.setEnabled(true);

					progressBar.setEnabled(false);
				}
			}
		});
		tglbtnSensor.setBounds(10, 177, 76, 28);
		add(tglbtnSensor);
		
		minValueComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int maximum = getMaxValue();
				int minimum = getMinValue();
				
				if(minimum > maximum) {
					minValueComboBoxModel.setSelectedItem(maximum);
				}
				updateValue();
			}
		});

		maxValueComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int maximum = getMaxValue();
				int minimum = getMinValue();

				if(minimum > maximum) {
					maxValueComboBoxModel.setSelectedItem(minimum);
				}
				updateValue();
			}

		});

	}
	
	/**
	 * Set the pin to control
	 * @param pin
	 */
	public void setPin(int pin) {
		pinComboBoxModel.setSelectedItem(pin);
	}

	public void setLink(Link link) {
		if (this.link != null && this.listener != null) {
			this.link.removeAnalogReadChangeListener(this.listener);
		}
		this.link = link;
	}

	public void setTitle(String title) {
		lblPowerPinController.setText(title);
	}
		
	public int getValue() {
		return Integer.parseInt((String)valueLabel.getText());
	}

	public void setValue(int value) {
		valueLabel.setText(String.valueOf(max(getMinValue(),
				min(value, getMaxValue()))));
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

}
