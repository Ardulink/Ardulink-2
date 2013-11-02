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

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

import org.zu.ardulink.Link;
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DisconnectionEvent;

public class ConnectionStatus extends JPanel implements ConnectionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6630818070505677116L;

	private JLabel lblStatelabel;
	
	private static final String CONNECTED = "connected";
	private static final String DISCONNECTED = "disconnected";
	
	private static final String CONNECTED_ICON_NAME = "/org/zu/ardulink/gui/icons/connect_established.png";
	private static final String DISCONNECTED_ICON_NAME = "/org/zu/ardulink/gui/icons/connect_no.png";
	
	private static final ImageIcon CONNECTED_ICON = new ImageIcon(ConnectionStatus.class.getResource(CONNECTED_ICON_NAME));
	private static final ImageIcon DISCONNECTED_ICON = new ImageIcon(ConnectionStatus.class.getResource(DISCONNECTED_ICON_NAME));

	/**
	 * Create the panel.
	 */
	public ConnectionStatus() {
		
		lblStatelabel = new JLabel(DISCONNECTED);
		lblStatelabel.setIcon(DISCONNECTED_ICON);
		add(lblStatelabel);

		Link.getDefaultInstance().addConnectionListener(this);
	}

	@Override
	public void connected(ConnectionEvent e) {
		lblStatelabel.setText(CONNECTED);
		lblStatelabel.setIcon(CONNECTED_ICON);
	}

	@Override
	public void disconnected(DisconnectionEvent e) {
		lblStatelabel.setText(DISCONNECTED);
		lblStatelabel.setIcon(DISCONNECTED_ICON);
	}

}
