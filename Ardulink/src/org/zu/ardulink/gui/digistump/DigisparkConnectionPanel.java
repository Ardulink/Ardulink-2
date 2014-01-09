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

package org.zu.ardulink.gui.digistump;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.zu.ardulink.Link;
import org.zu.ardulink.connection.usb.DigisparkUSBConnection;

public class DigisparkConnectionPanel extends JPanel {

	private static final long serialVersionUID = 6713040751827233041L;

	private Link link = Link.createInstance("digisparkConnection", new DigisparkUSBConnection());

	private JButton discoverButton;
	private JComboBox deviceComboBox;

	/**
	 * Create the panel.
	 */
	public DigisparkConnectionPanel() {
		Dimension dimension = new Dimension(275, 55);
		setPreferredSize(dimension);
		setMinimumSize(dimension);
		setLayout(null);

		discoverButton = new JButton("");
		discoverButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> portList = link.getPortList();
//				portList = new ArrayList<String>(); // Mock code...
//				portList.add("COM19");
//				portList.add("COM20");
				if(portList != null && portList.size() > 0) {
					deviceComboBox.setModel(new DefaultComboBoxModel(portList.toArray()));
				}
			}
		});
		discoverButton.setIcon(new ImageIcon(DigisparkConnectionPanel.class.getResource("/org/zu/ardulink/gui/icons/search_icon.png")));
		discoverButton.setToolTipText("Discover");
		discoverButton.setBounds(237, 9, 32, 32);
		add(discoverButton);
		
		JLabel lblDigispark = new JLabel("Digispark:");
		lblDigispark.setBounds(6, 17, 65, 16);
		add(lblDigispark);
		
		deviceComboBox = new JComboBox();
		deviceComboBox.setBounds(67, 12, 165, 26);
		add(deviceComboBox);
		
	}

	public Link getLink() {
		return link;
	}
	
	public String getSelectedDevice() {
		return (String)deviceComboBox.getSelectedItem();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		deviceComboBox.setEnabled(enabled);
		discoverButton.setEnabled(enabled);
	}
	
}
