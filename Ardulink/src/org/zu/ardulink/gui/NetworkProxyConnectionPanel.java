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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.zu.ardulink.Link;
import org.zu.ardulink.connection.proxy.NetworkProxy;
import org.zu.ardulink.connection.proxy.NetworkProxyServer;

public class NetworkProxyConnectionPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -519317364929658756L;
	
	private ConnectionPanel connectionPanel;
	private JTextField hostNameTextField;
	private JTextField hostPortTextField;
	private JButton activateButton;
	
	private Link link;
	
	/**
	 * Create the panel.
	 */
	public NetworkProxyConnectionPanel() {
		Dimension dimension = new Dimension(240, 390);
		setPreferredSize(dimension);
		setMinimumSize(dimension);
		setLayout(null);
		
		connectionPanel = new ConnectionPanel();
		connectionPanel.setLocation(0, 115);
		connectionPanel.setSize(connectionPanel.getPreferredSize());
		connectionPanel.setEnabled(false);
		add(connectionPanel);
		
		JLabel lblHostName = new JLabel("Host Name:");
		lblHostName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblHostName.setBounds(6, 12, 91, 16);
		add(lblHostName);
		
		hostNameTextField = new JTextField();
		hostNameTextField.setColumns(10);
		hostNameTextField.setBounds(108, 6, 122, 28);
		add(hostNameTextField);
		
		JLabel lblHostPort = new JLabel("Host Port:");
		lblHostPort.setHorizontalAlignment(SwingConstants.RIGHT);
		lblHostPort.setBounds(6, 45, 91, 16);
		add(lblHostPort);
		
		hostPortTextField = new JTextField();
		hostPortTextField.setColumns(10);
		hostPortTextField.setBounds(108, 39, 122, 28);
		hostPortTextField.setText("" + NetworkProxyServer.DEFAULT_LISTENING_PORT);
		add(hostPortTextField);
		
		activateButton = new JButton("Activate Proxy");
		activateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String hostName = hostNameTextField.getText();
				if(hostName == null || hostName.trim().equals("")) {
					hostName = "127.0.0.1";
					hostNameTextField.setText(hostName);
				}
				String hostPortString = hostPortTextField.getText();
				int hostPort = -1;
				try {
					hostPort = Integer.parseInt(hostPortString);
				}
				catch(NumberFormatException nfe) {
					JOptionPane.showMessageDialog(hostPortTextField, "Invalid host port. " + hostPortString, "Error", JOptionPane.ERROR_MESSAGE);
				}
				if(hostPort != -1) {
					try {
						// Create a NetworkProxy (the Connection implementation to send data over the net)
						// params are hostname and hostport
						NetworkProxy connection = new NetworkProxy(hostName, hostPort);
						
						// Create a Link class (so now we use this instead of the default one)
						link = Link.createInstance(hostPortTextField.getParent().toString(), connection);
						connectionPanel.setLink(link);
						
						connectionPanel.setEnabled(true);
						activateButton.setEnabled(false);
						hostNameTextField.setEnabled(false);
						hostPortTextField.setEnabled(false);
					}
					catch(Exception ex) {
						JOptionPane.showMessageDialog(hostPortTextField.getParent(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		activateButton.setBounds(5, 75, 230, 28);
		add(activateButton);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		boolean activated = (link != null);
		connectionPanel.setEnabled(enabled && activated);
		activateButton.setEnabled(enabled && !activated);
		hostNameTextField.setEnabled(enabled && !activated);
		hostPortTextField.setEnabled(enabled && !activated);
	}

	public Link getLink() {
		return link;
	}

	public Link destroyLink() {
		Link retvalue = Link.destroyInstance(this.toString());
		link = null;
		setEnabled(isEnabled());

		return retvalue;
	}

	public String getConnectionPort() {
		return connectionPanel.getConnectionPort();
	}

	public String getBaudRate() {
		return connectionPanel.getBaudRate();
	}
	
}
