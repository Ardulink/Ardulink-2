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

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.zu.ardulink.Link;
import org.zu.ardulink.event.DigitalReadChangeEvent;
import org.zu.ardulink.event.DigitalReadChangeListener;
import org.zu.ardulink.gui.facility.UtilityModel;

public class DigitalPinStatus extends JPanel implements DigitalReadChangeListener {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7773514191770737230L;

	private JLabel lblStatelabel;
	private JToggleButton tglbtnSensor;
	private JComboBox pinComboBox;
	private JLabel lblPin;
	
	private static final String HIGH = "High";
	private static final String LOW = "Low";
	
	private static final String HIGH_ICON_NAME = "/org/zu/ardulink/gui/icons/blue-on-32.png";
	private static final String LOW_ICON_NAME = "/org/zu/ardulink/gui/icons/blue-off-32.png";
	
	private static final ImageIcon HIGH_ICON = new ImageIcon(DigitalPinStatus.class.getResource(HIGH_ICON_NAME));
	private static final ImageIcon LOW_ICON = new ImageIcon(DigitalPinStatus.class.getResource(LOW_ICON_NAME));
	private JPanel comboPanel;

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
		
		pinComboBox = new JComboBox();
		comboPanel.add(pinComboBox);
		pinComboBox.setModel(new DefaultComboBoxModel(UtilityModel.generateModelForCombo(2, 13)));
		
		tglbtnSensor = new JToggleButton("Sensor off");
		tglbtnSensor.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					Link.getDefaultInstance().addDigitalReadChangeListener((DigitalReadChangeListener)tglbtnSensor.getParent());
					
					tglbtnSensor.setText("Sensor on");
					pinComboBox.setEnabled(false);
					
					lblStatelabel.setEnabled(true);
					
					
				} else if(e.getStateChange() == ItemEvent.DESELECTED) {
					Link.getDefaultInstance().removeDigitalReadChangeListener((DigitalReadChangeListener)tglbtnSensor.getParent());
					
					tglbtnSensor.setText("Sensor off");
					pinComboBox.setEnabled(true);

					lblStatelabel.setEnabled(false);
				}
			}
		});
		add(tglbtnSensor);
	}

	@Override
	public void stateChanged(DigitalReadChangeEvent e) {
		int value = e.getValue();
		if(value == DigitalReadChangeEvent.POWER_HIGH) {
			lblStatelabel.setText(HIGH);
			lblStatelabel.setIcon(HIGH_ICON);
		} else if(value == DigitalReadChangeEvent.POWER_LOW) {
			lblStatelabel.setText(LOW);
			lblStatelabel.setIcon(LOW_ICON);
		}
	}

	@Override
	public int getPinListening() {
		return Integer.parseInt((String)pinComboBox.getSelectedItem());
	}

	public void setPin(int pin) {
		pinComboBox.setSelectedItem("" + pin);
	}
}
