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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.zu.ardulink.Link;
import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;
import org.zu.ardulink.gui.facility.UtilityModel;
import org.zu.ardulink.protocol.ReplyMessageCallback;

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
public class AnalogPinStatus extends JPanel implements Linkable, AnalogReadChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7927439571760351922L;
	private JLabel valueLabel;
	private JLabel voltValueLbl;
	private JProgressBar progressBar;
	private JComboBox maxValueComboBox;
	private JComboBox minValueComboBox;
	private JComboBox pinComboBox;
	private JLabel lblPowerPinController;
	private JToggleButton tglbtnSensor;

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
		
		pinComboBox = new JComboBox();
		// TODO definire un metodo per poter cambiare l'insieme dei pin controllabili. In questo modo si può lavorare anche con schede diverse da Arduino UNO
		// pinComboBox.setModel(new DefaultComboBoxModel(new String[] {"3", "5", "6", "9", "10", "11"}));
		pinComboBox.setModel(new DefaultComboBoxModel(UtilityModel.generateModelForCombo(0, 40)));
		pinComboBox.setSelectedItem("0");
		pinComboBox.setBounds(65, 36, 62, 22);
		add(pinComboBox);
		
		maxValueComboBox = new JComboBox();
		maxValueComboBox.setModel(new DefaultComboBoxModel(UtilityModel.generateModelForCombo(0, 1023)));
		maxValueComboBox.setBounds(65, 65, 62, 22);
		maxValueComboBox.setSelectedItem("1023");
		add(maxValueComboBox);

		minValueComboBox = new JComboBox();
		minValueComboBox.setModel(new DefaultComboBoxModel(UtilityModel.generateModelForCombo(0, 1023)));
		minValueComboBox.setBounds(65, 217, 62, 22);
		minValueComboBox.setSelectedItem("0");
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
					link.addAnalogReadChangeListener((AnalogReadChangeListener)tglbtnSensor.getParent());
					
					tglbtnSensor.setText("On");
					pinComboBox.setEnabled(false);
					minValueComboBox.setEnabled(false);
					maxValueComboBox.setEnabled(false);
					
					progressBar.setEnabled(true);
					
					
				} else if(e.getStateChange() == ItemEvent.DESELECTED) {
					link.removeAnalogReadChangeListener((AnalogReadChangeListener)tglbtnSensor.getParent());
					
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
					minimum = maximum;
					minValueComboBox.setSelectedItem("" + minimum);
				}
				updateVale();
			}
		});

		maxValueComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int maximum = getMaxValue();
				int minimum = getMinValue();

				if(minimum > maximum) {
					maximum = minimum;
					maxValueComboBox.setSelectedItem("" + maximum);
				}
				
				updateVale();
			}

		});

	}
	
	/**
	 * Set the pin to control
	 * @param pin
	 */
	public void setPin(int pin) {
		pinComboBox.setSelectedItem("" + pin);
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public ReplyMessageCallback getReplyMessageCallback() {
		throw new RuntimeException("Not developed yet");
	}

	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		throw new RuntimeException("Not developed yet");
	}
	
	public void setTitle(String title) {
		lblPowerPinController.setText(title);
	}
		
	public int getValue() {
		return Integer.parseInt((String)valueLabel.getText());
	}

	public void setValue(int value) {
		int maxValue = getMaxValue();
		int minValue = getMinValue();
		if(value > maxValue) {
			value = maxValue;
		} else if(value < minValue) {
			value = minValue;
		}
		valueLabel.setText(Integer.toString(value));
	}

	public int getMinValue() {
		return Integer.parseInt((String)minValueComboBox.getSelectedItem());
	}
	
	public int getMaxValue() {
		return Integer.parseInt((String)maxValueComboBox.getSelectedItem());
	}
	
	private void updateVale() {
		setValue(getValue());
	}

	@Override
	public void stateChanged(AnalogReadChangeEvent e) {
		int value = e.getValue();
		valueLabel.setText(Integer.toString(value));

        float volt = ((float)(((float)value)*5.0f))/1023.0f;
        voltValueLbl.setText(""+volt+"V");

		float progress  = ((float)(((float)(value - getMinValue()))*100.0f))/((float)getMaxValue() - (float)getMinValue());
        progressBar.setValue((int)progress);
		
	}

	@Override
	public int getPinListening() {
		return Integer.parseInt(((String)pinComboBox.getSelectedItem()));
	}
}
