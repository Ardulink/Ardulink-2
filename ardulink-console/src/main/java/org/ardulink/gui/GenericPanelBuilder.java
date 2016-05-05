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

import static java.awt.GridBagConstraints.REMAINDER;
import static org.ardulink.gui.GridBagConstraintsBuilder.constraints;
import static org.ardulink.util.Primitive.parseAs;
import static org.ardulink.util.Primitive.unwrap;
import static org.ardulink.util.Primitive.wrap;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URI;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.linkmanager.LinkManager.NumberValidationInfo;
import org.ardulink.core.linkmanager.LinkManager.ValidationInfo;
import org.ardulink.util.Primitive;

public class GenericPanelBuilder implements PanelBuilder {

	@Override
	public boolean canHandle(URI uri) {
		// we can handle all URIs
		return true;
	}

	@Override
	public JPanel createPanel(Configurer configurer) {
		JPanel panel = new JPanel(new GridBagLayout());
		int row = 0;
		for (String name : configurer.getAttributes()) {
			int col = 0;
			ConfigAttribute attribute = configurer.getAttribute(name);
			String description = attribute.getDescription();
			JLabel label = new JLabel(attribute.getName());
			label.setToolTipText(description);
			panel.add(label, constraints(row, col++).build());

			boolean isDiscoverable = attribute.choiceDependsOn().length > 0;

			JComponent component = createComponent(attribute);
			component.setToolTipText(description);
			panel.add(
					component,
					constraints(row, col++)
							.gridwidth(isDiscoverable ? 1 : REMAINDER)
							.fillHorizontal().build());

			Component comp = isDiscoverable ? createDiscoverButton(attribute,
					component) : new JPanel();
			panel.add(comp, constraints(row, col++).build());
			row++;
		}
		panel.add(new JPanel(), constraints(row++, 0).gridwidth(REMAINDER)
				.fillBoth().build());
		return panel;
	}

	private static JButton createDiscoverButton(
			final ConfigAttribute attribute, final JComponent component) {
		JButton discoverButton = new JButton(loadIcon());
		discoverButton.setToolTipText("Discover");
		discoverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (component instanceof JComboBox) {
					JComboBox jComboBox = (JComboBox) component;
					ComboBoxModel model = new DefaultComboBoxModel(attribute
							.getChoiceValues());
					jComboBox.setModel(model);
				}
			}
		});
		return discoverButton;
	}

	private static ImageIcon loadIcon() {
		return new ImageIcon(
				GenericPanelBuilder.class.getResource("icons/search_icon.png"));
	}

	private static JComponent createComponent(final ConfigAttribute attribute) {
		if (isBoolean(attribute)) {
			final JCheckBox checkBox = new JCheckBox();
			checkBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					attribute.setValue(Boolean.valueOf(checkBox.isSelected()));
				}
			});
			return setState(checkBox, attribute);
		} else if (isChoice(attribute)) {
			final JComboBox jComboBox = new JComboBox(
					attribute.getChoiceValues());
			jComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					attribute.setValue(jComboBox.getSelectedItem());
				}
			});
			return selectFirstValue(jComboBox);
		} else if (isNumber(attribute)) {
			final JSpinner spinner = new JSpinner(createModel(attribute));
			JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
			editor.getTextField().setHorizontalAlignment(
					JFormattedTextField.LEFT);
			editor.getFormat().setGroupingUsed(false);
			spinner.setEditor(editor);
			spinner.setValue(attribute.getValue());

			spinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					attribute.setValue(parseAs(unwrap(attribute.getType()),
							String.valueOf(spinner.getValue())));
				}
			});
			return spinner;
		} else {
			Object value = attribute.getValue();
			final JTextField jTextField = new JTextField(value == null ? ""
					: String.valueOf(value));

			jTextField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					attribute.setValue(jTextField.getText());
				}
			});
			return jTextField;
		}
	}

	private static SpinnerModel createModel(ConfigAttribute attribute) {
		ValidationInfo info = attribute.getValidationInfo();
		if (info instanceof NumberValidationInfo) {
			NumberValidationInfo nInfo = (NumberValidationInfo) info;
			if (wrap(attribute.getType()).equals(Integer.class)) {
				return new SpinnerNumberModel((int) nInfo.min(),
						(int) nInfo.min(), (int) nInfo.max(), 1);
			}
			return new SpinnerNumberModel(nInfo.min(), nInfo.min(),
					nInfo.max(), 1);
		}
		return new SpinnerNumberModel();
	}

	private static JComponent setState(JCheckBox checkBox,
			ConfigAttribute attribute) {
		checkBox.setSelected(Boolean.valueOf((Boolean) attribute.getValue()));
		return checkBox;
	}

	private static JComponent selectFirstValue(JComboBox comboBox) {
		comboBox.setSelectedIndex(comboBox.getModel().getSize() > 0 ? 0 : -1);
		return comboBox;
	}

	private static boolean isChoice(ConfigAttribute attribute) {
		return attribute.hasChoiceValues();
	}

	private static boolean isBoolean(ConfigAttribute attribute) {
		return attribute.getType().equals(Boolean.class)
				|| attribute.getType().equals(boolean.class);
	}

	private static boolean isNumber(ConfigAttribute attribute) {
		return Number.class
				.isAssignableFrom(Primitive.wrap(attribute.getType()));
	}

}
