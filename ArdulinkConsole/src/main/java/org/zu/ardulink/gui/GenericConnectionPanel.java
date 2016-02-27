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

import static com.github.pfichtner.ardulink.core.linkmanager.LinkManager.extractNameFromURI;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.event.ItemEvent.SELECTED;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.zu.ardulink.legacy.Link;
import org.zu.ardulink.util.Primitive;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.NumberValidationInfo;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ValidationInfo;

public class GenericConnectionPanel extends JPanel implements Linkable {

	private static final long serialVersionUID = 1290277902714226253L;

	private final JComboBox uris = createURICombo();

	private JComboBox createURICombo() {
		JComboBox uris = new JComboBox();
		uris.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 2756587449741341859L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				try {
					return super.getListCellRendererComponent(list,
							extractNameFromURI(new URI((String) value)), index,
							isSelected, cellHasFocus);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			}
		});
		uris.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == SELECTED) {
					subPanel.removeAll();
					String selectedItem = (String) event.getItem();
					try {
						Configurer configurer = LinkManager.getInstance()
								.getConfigurer(new URI(selectedItem));
						int row = 0;
						for (String name : configurer.getAttributes()) {
							final ConfigAttribute attribute = configurer
									.getAttribute(name);
							subPanel.add(new JLabel(attribute.getName()),
									constraints(row, 0));

							boolean isDiscoverable = attribute
									.choiceDependsOn().length > 0;

							GridBagConstraints c = constraints(row, 1);
							c.weightx = 1;
							c.fill = HORIZONTAL;
							c.gridwidth = isDiscoverable ? 1 : REMAINDER;
							final JComponent component = createComponent(attribute);
							subPanel.add(component, c);

							if (isDiscoverable) {
								JButton discoverButton = new JButton(
										new ImageIcon(
												BluetoothConnectionPanel.class
														.getResource("icons/search_icon.png")));
								discoverButton.setToolTipText("Discover");
								discoverButton
										.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(
													ActionEvent e) {
												if (component instanceof JComboBox) {
													JComboBox jComboBox = (JComboBox) component;
													ComboBoxModel model = new DefaultComboBoxModel(
															attribute
																	.getChoiceValues());
													jComboBox.setModel(model);
												}
											}
										});

								subPanel.add(discoverButton,
										constraints(row, 2));
							} else
								subPanel.add(new JPanel(), constraints(row, 2));

							row++;
						}
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
					subPanel.repaint();
				}
			}

			private GridBagConstraints constraints(int row, int x) {
				GridBagConstraints c = new GridBagConstraints();
				c.anchor = LINE_START;
				c.gridx = x;
				c.gridy = row;
				c.insets = new Insets(4, 4, 0, 0);
				return c;
			}

			private JComponent createComponent(ConfigAttribute attribute) {
				if (isBoolean(attribute)) {
					return setState(new JCheckBox(), attribute);
				} else if (isChoice(attribute)) {
					final JComboBox jComboBox = new JComboBox(attribute
							.getChoiceValues());
					return selectFirstValue(jComboBox);
				} else if (isNumber(attribute)) {
					JSpinner spinner = new JSpinner(createModel(attribute));
					JSpinner.NumberEditor editor = new JSpinner.NumberEditor(
							spinner);
					editor.getFormat().setGroupingUsed(false);
					spinner.setEditor(editor);
					spinner.setValue(attribute.getValue());
					return spinner;
				} else {
					Object value = attribute.getValue();
					return new JTextField(value == null ? "" : String
							.valueOf(value));
				}
			}

			private SpinnerModel createModel(ConfigAttribute attribute) {
				ValidationInfo info = attribute.getValidationInfo();
				if (info instanceof NumberValidationInfo) {
					NumberValidationInfo nInfo = (NumberValidationInfo) info;
					return new SpinnerNumberModel(nInfo.min(), nInfo.min(),
							nInfo.max(), 1);
				}
				return new SpinnerNumberModel();
			}

			private JComponent setState(JCheckBox checkBox,
					ConfigAttribute attribute) {
				checkBox.setSelected(Boolean.valueOf((Boolean) attribute
						.getValue()));
				return checkBox;
			}

			private JComponent selectFirstValue(JComboBox comboBox) {
				comboBox.setSelectedIndex(comboBox.getModel().getSize() > 0 ? 0
						: -1);
				return comboBox;
			}

			private boolean isChoice(ConfigAttribute attribute) {
				return attribute.hasChoiceValues();
			}

			private boolean isBoolean(ConfigAttribute attribute) {
				return attribute.getType().equals(Boolean.class)
						|| attribute.getType().equals(boolean.class);
			}

			private boolean isNumber(ConfigAttribute attribute) {
				return Number.class.isAssignableFrom(Primitive.wrap(attribute
						.getType()));
			}

		});
		return uris;
	}

	private Link link;

	private JPanel subPanel;

	/**
	 * Create the panel.
	 */
	public GenericConnectionPanel() {
		setLayout(new BorderLayout());
		add(new JLabel("URI"), BorderLayout.WEST);
		add(uris, BorderLayout.EAST);
		this.subPanel = subPanel();
		LinkManager linkManager = LinkManager.getInstance();
		for (URI uri : linkManager.listURIs()) {
			uris.addItem(uri.toASCIIString());
		}
		add(subPanel, BorderLayout.SOUTH);
	}

	private JPanel subPanel() {
		JPanel subPanel = new JPanel();
		GridBagLayout mgr = new GridBagLayout();
		subPanel.setLayout(mgr);
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

	@Override
	public void setLink(Link link) {
		this.link = link;
	}

	public void setBaudRateVisible(boolean visibility) {
		uris.setVisible(visibility);
	}
}
