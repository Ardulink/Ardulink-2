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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.zu.ardulink.Link;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * This component is Able to search for serial ports connected to the Arduino and select one.
 * You can also specify a baud rate.
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class SerialConnectionPanel extends JPanel implements Linkable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1290277902714226253L;
	private JComboBox connectionPortComboBox;
	private JTextField baudRateTextField;
	private JButton discoverButton;
	private JLabel lblBaudRate;
	
	private Link link = Link.getDefaultInstance();
	
	/**
	 * Create the panel.
	 */
	public SerialConnectionPanel() {

		// decomment this to use simple byte protocol
//		link = Link.getInstance("serialConnection");
//		if(link == null) {
//			Set<String> protocolNames = ProtocolHandler.getInstalledProtocolImplementationNames();
//			if(!protocolNames.contains(SimpleBinaryProtocol.NAME)) {
//				ProtocolHandler.installProtocolImplementation(new SimpleBinaryProtocol());
//			}
//			link = Link.createInstance("serialConnection", SimpleBinaryProtocol.NAME);
//		}
		
		Dimension dimension = new Dimension(275, 80);
		setPreferredSize(dimension);
		setMinimumSize(dimension);
		setLayout(null);
		
		JLabel connectionPortLabel = new JLabel("Connection Port:");
		connectionPortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		connectionPortLabel.setBounds(6, 16, 91, 16);
		add(connectionPortLabel);
		
		connectionPortComboBox = new JComboBox();
		connectionPortComboBox.setBounds(108, 10, 122, 28);
		add(connectionPortComboBox);
		
		lblBaudRate = new JLabel("Baud Rate:");
		lblBaudRate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBaudRate.setBounds(6, 44, 91, 16);
		add(lblBaudRate);
		
		baudRateTextField = new JTextField();
		baudRateTextField.setText("" + Link.DEFAULT_BAUDRATE);
		baudRateTextField.setColumns(10);
		baudRateTextField.setBounds(108, 38, 122, 28);
		add(baudRateTextField);
		
		discoverButton = new JButton("");
		discoverButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> portList = link.getPortList();
//				portList = new ArrayList<String>(); // Mock code...
//				portList.add("COM19");
//				portList.add("COM20");
				if(portList != null && portList.size() > 0) {
					connectionPortComboBox.setModel(new DefaultComboBoxModel(portList.toArray()));
				} else {
					connectionPortComboBox.removeAllItems();
				}
			}
		});
		discoverButton.setIcon(new ImageIcon(SerialConnectionPanel.class.getResource("/org/zu/ardulink/gui/icons/search_icon.png")));
		discoverButton.setToolTipText("Discover");
		discoverButton.setBounds(235, 8, 32, 32);
		add(discoverButton);
		

	}
	
	/**
	 * @return the connection port name selected or set
	 */
	public String getConnectionPort() {
		String retvalue = "";
		Object selectedItem = connectionPortComboBox.getSelectedItem();
		if(selectedItem != null) {
			retvalue = selectedItem.toString();
		}
		return retvalue;
	}

	/**
	 * @return the baud rate set
	 */
	//TODO if not numeric take default from Link class.
	public String getBaudRate() {
		return baudRateTextField.getText();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		connectionPortComboBox.setEnabled(enabled);
		baudRateTextField.setEnabled(enabled);
		discoverButton.setEnabled(enabled);
	}

	public Link getLink() {
		return link;
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
	
	public void setBaudRateVisible(boolean visibility) {
		baudRateTextField.setVisible(visibility);
		lblBaudRate.setVisible(visibility);
	}
}
