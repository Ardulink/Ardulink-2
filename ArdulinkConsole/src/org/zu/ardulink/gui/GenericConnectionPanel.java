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
import java.awt.FlowLayout;
import java.net.URI;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.zu.ardulink.legacy.Link;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;

public class GenericConnectionPanel extends JPanel implements Linkable {

	private static final long serialVersionUID = 1290277902714226253L;

	private JTextField uri;

	private Link link;

	/**
	 * Create the panel.
	 */
	public GenericConnectionPanel() {
		setLayout(new FlowLayout());

		JLabel connectionPortLabel = new JLabel("URI");
		add(connectionPortLabel);

		uri = new JTextField();
		List<URI> listURIs = LinkManager.getInstance().listURIs();
		if (!listURIs.isEmpty()) {
			uri.setText(listURIs.get(0).toASCIIString());
		}
		add(uri);
	}

	public String getURI() {
		return uri.getText();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		uri.setEnabled(enabled);
	}

	private Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public void setBaudRateVisible(boolean visibility) {
		uri.setVisible(visibility);
	}
}
