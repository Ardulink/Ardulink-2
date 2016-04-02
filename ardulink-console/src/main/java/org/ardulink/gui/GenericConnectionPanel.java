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

import static org.ardulink.core.linkmanager.LinkManager.extractNameFromURI;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.event.ItemEvent.SELECTED;

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
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.ardulink.legacy.Link;
import org.ardulink.util.Primitive;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.linkmanager.LinkManager.NumberValidationInfo;
import org.ardulink.core.linkmanager.LinkManager.ValidationInfo;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
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
					clean();
					String selectedItem = (String) event.getItem();
					try {
						Configurer configurer = LinkManager.getInstance()
								.getConfigurer(new URI(selectedItem));
						int row = 1;
						for (String name : configurer.getAttributes()) {
							final ConfigAttribute attribute = configurer
									.getAttribute(name);
							add(new JLabel(attribute.getName()),
									constraints(row, 0));

							boolean isDiscoverable = attribute
									.choiceDependsOn().length > 0;

							GridBagConstraints c = makeFill(constraints(row, 1));
							c.gridwidth = isDiscoverable ? 1 : REMAINDER;
							final JComponent component = createComponent(attribute);
							add(component, c);

							if (isDiscoverable) {
								JButton discoverButton = new JButton(
										new ImageIcon(getClass().getResource(
												"icons/search_icon.png")));
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

								add(discoverButton, constraints(row, 2));
							} else {
								add(new JPanel(), constraints(row, 2));
							}

							row++;
						}
						GridBagConstraints c = constraints(row, 0);
						c.gridwidth = 3;
						c.weighty = 1;
						add(new JPanel(), c);

					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
					revalidate();
				}
			}

			private void clean() {
				Component[] components = getComponents();
				for (int i = fixedComponents; i < components.length; i++) {
					remove(components[i]);
				}
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
					editor.getTextField().setHorizontalAlignment(
							JFormattedTextField.LEFT);
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

	private GridBagConstraints constraints(int row, int column) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = LINE_START;
		c.gridx = column;
		c.gridy = row;
		c.insets = new Insets(4, 4, 0, 0);
		return c;
	}

	private GridBagConstraints makeFill(GridBagConstraints c) {
		c.weightx = 1;
		c.fill = HORIZONTAL;
		return c;
	}

	private Link link;

	private final int fixedComponents;

	/**
	 * Create the panel.
	 */
	public GenericConnectionPanel() {
		setLayout(new GridBagLayout());
		add(new JLabel("Type"), constraints(0, 0));
		GridBagConstraints c = constraints(0, 1);
		c.gridwidth = 2;
		add(uris, makeFill(c));
		add(new JPanel(), constraints(0, 2));
		this.fixedComponents = getComponentCount();
		LinkManager linkManager = LinkManager.getInstance();
		for (URI uri : linkManager.listURIs()) {
			uris.addItem(uri.toASCIIString());
		}
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
