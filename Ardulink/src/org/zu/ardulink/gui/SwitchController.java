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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.zu.ardulink.Link;
import org.zu.ardulink.gui.facility.UtilityModel;
import org.zu.ardulink.protocol.IProtocol;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * This class can manage digital arduino pins sending specific messages to
 * the arduino board.
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class SwitchController extends JPanel implements Linkable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -260988038687002762L;

	private JComboBox pinComboBox;
	private JToggleButton switchToggleButton;
	
	private Link link = Link.getDefaultInstance();

	/**
	 * Create the panel.
	 */
	public SwitchController() {
		setPreferredSize(new Dimension(125, 75));
		setLayout(null);
		pinComboBox = new JComboBox();
		// TODO definire un metodo per poter cambiare l'insieme dei pin controllabili. In questo modo si può lavorare anche con schede diverse da Arduino UNO
		pinComboBox.setModel(new DefaultComboBoxModel(UtilityModel.generateModelForCombo(0, 13)));
		pinComboBox.setSelectedItem("3");
		pinComboBox.setBounds(66, 11, 47, 22);
		add(pinComboBox);
		
		JLabel label = new JLabel("Power Pin:");
		label.setFont(new Font("SansSerif", Font.PLAIN, 11));
		label.setBounds(10, 15, 59, 14);
		add(label);
		
		switchToggleButton = new JToggleButton("Off");
		switchToggleButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					
					switchToggleButton.setText("On");
					
					int pin = Integer.parseInt((String)pinComboBox.getSelectedItem());
					link.sendPowerPinSwitch(pin, IProtocol.POWER_HIGH);
				} else if(e.getStateChange() == ItemEvent.DESELECTED) {
					
					switchToggleButton.setText("Off");
					
					int pin = Integer.parseInt((String)pinComboBox.getSelectedItem());
					link.sendPowerPinSwitch(pin, IProtocol.POWER_LOW);
				}
			}
		});
		switchToggleButton.setBounds(10, 38, 103, 23);
		add(switchToggleButton);
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

}
