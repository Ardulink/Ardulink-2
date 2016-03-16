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

package org.zu.ardulink.gui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.zu.ardulink.legacy.Link;

import com.github.pfichtner.ardulink.core.ConnectionListener;

/**
 * [ardulinktitle] [ardulinkversion] This component listens for connection or
 * disconnection events showing the state of a Link in the GUI.
 * 
* project Ardulink http://www.ardulink.org/
 * @see ConnectionListener [adsense]
 *
 */
public class ConnectionStatus extends JPanel implements ConnectionListener, Linkable {

	private static final long serialVersionUID = 6630818070505677116L;

	private JLabel lblStatelabel;

	private static final String CONNECTED = "connected";
	private static final String DISCONNECTED = "disconnected";

	private static final String CONNECTED_ICON_NAME = "icons/connect_established.png";
	private static final String DISCONNECTED_ICON_NAME = "icons/connect_no.png";

	private static final ImageIcon CONNECTED_ICON = new ImageIcon(
			ConnectionStatus.class.getResource(CONNECTED_ICON_NAME));
	private static final ImageIcon DISCONNECTED_ICON = new ImageIcon(
			ConnectionStatus.class.getResource(DISCONNECTED_ICON_NAME));

	private Link link;

	/**
	 * Create the panel.
	 */
	public ConnectionStatus() {
		lblStatelabel = new JLabel();
		connectionLost();
		add(lblStatelabel);
	}

	@Override
	public void connectionLost() {
		lblStatelabel.setText(DISCONNECTED);
		lblStatelabel.setIcon(DISCONNECTED_ICON);
	}

	@Override
	public void reconnected() {
		lblStatelabel.setText(CONNECTED);
		lblStatelabel.setIcon(CONNECTED_ICON);
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}

}
