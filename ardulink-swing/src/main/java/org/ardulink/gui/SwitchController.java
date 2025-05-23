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
import static org.ardulink.util.Preconditions.checkNotNull;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.ardulink.core.Link;
import org.ardulink.gui.facility.IntMinMaxModel;
import org.ardulink.util.Throwables;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * This class can manage digital arduino pins sending specific messages to the
 * arduino board.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class SwitchController extends JPanel implements Linkable {

	private static final long serialVersionUID = -260988038687002762L;

	private final JComboBox<Integer> pinComboBox;
	private final IntMinMaxModel pinComboBoxModel;
	private final JToggleButton switchToggleButton;
	
	private transient Link link;

	/**
	 * Create the panel.
	 */
	public SwitchController() {
		setPreferredSize(new Dimension(125, 75));
		setLayout(null);
		pinComboBoxModel = new IntMinMaxModel(0, 40).withSelectedItem(3);
		pinComboBox = new JComboBox<>(pinComboBoxModel);
		pinComboBox.setName("pinComboBox");
		pinComboBox.setBounds(66, 11, 47, 22);
		add(pinComboBox);
		
		JLabel label = new JLabel("Power Pin:");
		label.setFont(new Font("SansSerif", Font.PLAIN, 11));
		label.setBounds(10, 15, 59, 14);
		add(label);
		
		switchToggleButton = new JToggleButton("Off");
		switchToggleButton.addItemListener(e -> {
			try {
				int pin = pinComboBoxModel.getSelectedItem().intValue();
				if(e.getStateChange() == ItemEvent.SELECTED) {
					switchToggleButton.setText("On");
					link.switchDigitalPin(digitalPin(pin), true);
				} else if(e.getStateChange() == ItemEvent.DESELECTED) {
					switchToggleButton.setText("Off");
					link.switchDigitalPin(digitalPin(pin), false);
				}
			} catch (IOException ex) {
				throw Throwables.propagate(ex);
			}
		});
		switchToggleButton.setBounds(10, 38, 103, 23);
		add(switchToggleButton);
	}
	
	/**
	 * Set the pin to control.
	 * 
	 * @param pin
	 * @return 
	 */
	public SwitchController setPin(int pin) {
		pinComboBox.setSelectedItem(pin);
		return this;
	}

	@Override
	public void setLink(Link link) {
		this.link = checkNotNull(link, "link must not be null");
	}

}
