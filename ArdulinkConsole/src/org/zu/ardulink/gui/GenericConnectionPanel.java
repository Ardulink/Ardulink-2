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

import static java.awt.event.ItemEvent.SELECTED;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.zu.ardulink.legacy.Link;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public class GenericConnectionPanel extends JPanel implements Linkable {

	private static final long serialVersionUID = 1290277902714226253L;

	private final JComboBox uris = new JComboBox();

	private Link link;

	private JPanel subPanel;

	/**
	 * Create the panel.
	 */
	public GenericConnectionPanel() {
		setLayout(new BorderLayout());
		add(new JLabel("URI"), BorderLayout.WEST);
		uris.addItemListener(itemListener());
		add(uris, BorderLayout.EAST);
		subPanel = subPanel();
		for (URI uri : LinkManager.getInstance().listURIs()) {
			uris.addItem(uri.toASCIIString());
		}
		add(subPanel, BorderLayout.SOUTH);
	}

	private ItemListener itemListener() {
		return new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == SELECTED) {
					subPanel.removeAll();
					String selectedItem = (String) event.getItem();
					try {
						Configurer configurer = LinkManager.getInstance()
								.getConfigurer(new URI(selectedItem));
						for (String name : configurer.getAttributes()) {
							ConfigAttribute attribute = configurer
									.getAttribute(name);
							subPanel.add(new JLabel(attribute.getName()));
							if (attribute.getType().equals(Boolean.class)
									|| attribute.getType()
											.equals(boolean.class)) {
								JCheckBox checkBox = new JCheckBox();
								checkBox.setSelected(Boolean
										.valueOf((Boolean) attribute.getValue()));
								subPanel.add(checkBox);
							} else if (attribute.hasChoiceValues()) {
								JComboBox comboBox = new JComboBox(
										attribute.getChoiceValues());
								if (comboBox.getModel().getSize() > 0) {
									comboBox.setSelectedIndex(0);
								}
								subPanel.add(comboBox);
							} else {
								subPanel.add(new JTextField(String
										.valueOf(attribute.getValue())));
							}
						}
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}

				}
			}
		};
	}

	private JPanel subPanel() {
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new GridLayout(10, 2));
		return subPanel;
	}

	public String getURI() {
		return uris.getSelectedItem().toString();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		uris.setEnabled(enabled);
	}

	private Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public void setBaudRateVisible(boolean visibility) {
		uris.setVisible(visibility);
	}
}
