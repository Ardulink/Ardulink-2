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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ardulink.core.ConnectionListener;
import org.ardulink.legacy.Link;

/**
 * [ardulinktitle] [ardulinkversion] This component listens for connection or
 * disconnection events showing the state of a Link in the GUI.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * @see ConnectionListener [adsense]
 *
 */
public class ConnectionStatus extends JPanel implements Linkable {

	private static final String ICON_FOLDER = "icons/";

	private static final long serialVersionUID = 6630818070505677116L;

	private JLabel lblStatelabel;

	private static final String CONNECTED_TEXT = "connected";
	private static final String DISCONNECTED_TEXT = "disconnected";

	private static final ImageIcon CONNECTED_ICON = loadIcon("connect_established.png");
	private static final ImageIcon DISCONNECTED_ICON = loadIcon("connect_no.png");

	private static ImageIcon loadIcon(String iconName) {
		return new ImageIcon(ConnectionStatus.class.getResource(ICON_FOLDER
				+ iconName));
	}

	private transient Link link = Link.NO_LINK;

	private final transient ConnectionListener connectionListener = new ConnectionListener() {

		@Override
		public void connectionLost() {
			lblStatelabel.setText(DISCONNECTED_TEXT);
			lblStatelabel.setIcon(DISCONNECTED_ICON);
		}

		@Override
		public void reconnected() {
			lblStatelabel.setText(CONNECTED_TEXT);
			lblStatelabel.setIcon(CONNECTED_ICON);
		}

	};

	/**
	 * Create the panel.
	 */
	public ConnectionStatus() {
		lblStatelabel = new JLabel();
		this.connectionListener.connectionLost();
		add(lblStatelabel);
	}

	@Override
	public void setLink(Link newLink) {
		if (this.link != null) {
			this.link.removeConnectionListener(this.connectionListener);
		}
		this.link = newLink;
		if (this.link != null) {
			this.link.addConnectionListener(this.connectionListener);
		}
		if (this.link == null || this.link == Link.NO_LINK) {
			connectionListener.connectionLost();
		} else {
			connectionListener.reconnected();
		}
	}

}
