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
package org.ardulink.gui.connectionpanel;

import static java.awt.GridBagConstraints.REMAINDER;
import static java.util.Arrays.asList;
import static org.ardulink.gui.connectionpanel.GridBagConstraintsBuilder.constraints;
import static org.ardulink.util.Numbers.numberType;
import static org.ardulink.util.Primitives.wrap;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URI;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.linkmanager.LinkManager.NumberValidationInfo;
import org.ardulink.core.linkmanager.LinkManager.ValidationInfo;
import org.ardulink.util.Numbers;
import org.ardulink.util.Primitives;

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
			panel.add(component,
					constraints(row, col++).gridwidth(isDiscoverable ? 1 : REMAINDER).fillHorizontal().build());

			@SuppressWarnings("unchecked")
			Component comp = isDiscoverable ? createDiscoverButton(attribute, (JComboBox<Object>) component)
					: new JPanel();
			panel.add(comp, constraints(row, col++).build());
			row++;
		}
		panel.add(new JPanel(), constraints(row++, 0).gridwidth(REMAINDER).fillBoth().build());
		return panel;
	}

	private static JButton createDiscoverButton(ConfigAttribute attribute, JComboBox<Object> comboBox) {
		JButton discoverButton = new JButton(loadIcon());
		discoverButton.setToolTipText("Discover");
		discoverButton
				.addActionListener(__ -> comboBox.setModel(new DefaultComboBoxModel<>(attribute.getChoiceValues())));
		return discoverButton;
	}

	private static ImageIcon loadIcon() {
		return new ImageIcon(GenericPanelBuilder.class.getResource("icons/search_icon.png"));
	}

	private static JComponent createComponent(ConfigAttribute attribute) {
		if (isBoolean(attribute)) {
			return createCheckBox(attribute);
		} else if (isChoice(attribute)) {
			return createComboxBox(attribute);
		} else if (isNumber(attribute)) {
			return createSpinner(attribute);
		} else {
			return createTextField(attribute);
		}
	}

	private static JComponent createCheckBox(ConfigAttribute attribute) {
		JCheckBox checkBox = new JCheckBox();
		checkBox.addActionListener(__ -> attribute.setValue(checkBox.isSelected()));
		return setState(checkBox, attribute);
	}

	private static JComponent createComboxBox(ConfigAttribute attribute) {
		JComboBox<Object> jComboBox = new JComboBox<>(attribute.getChoiceValues());
		if (attribute.getType().isEnum()) {
			ListCellRenderer<Object> delegate = jComboBox.getRenderer();
			jComboBox.setRenderer(
					(list, value, index, isSelected, cellHasFocus) -> delegate.getListCellRendererComponent(list,
							value == null ? null : resolveText(attribute, value), index, isSelected, cellHasFocus));
		}
		jComboBox.addActionListener(__ -> attribute.setValue(jComboBox.getSelectedItem()));
		boolean nullIsAvalidItem = asList(attribute.getChoiceValues()).contains(null);
		// raise a selection event on model changes
		jComboBox.addPropertyChangeListener("model",
				__ -> setSelection(jComboBox, attribute.getValue(), nullIsAvalidItem));
		setSelection(jComboBox, attribute.getValue(), nullIsAvalidItem);
		return jComboBox;
	}

	private static Object resolveText(ConfigAttribute attribute, Object value) {
		String choiceDescription = attribute.getChoiceDescription(value);
		return choiceDescription == null ? value : choiceDescription;
	}

	private static JComponent createSpinner(ConfigAttribute attribute) {
		JSpinner spinner = new JSpinner(createModel(attribute));
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
		editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
		editor.getFormat().setGroupingUsed(false);
		spinner.setEditor(editor);
		spinner.setValue(attribute.getValue());
		spinner.addChangeListener(__ -> attribute.setValue(spinner.getValue()));
		return spinner;
	}

	private static JComponent createTextField(ConfigAttribute attribute) {
		Object value = attribute.getValue();
		JTextField jTextField = new JTextField(value == null ? "" : String.valueOf(value));

		jTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				attribute.setValue(jTextField.getText());
			}
		});
		return jTextField;
	}

	@SuppressWarnings("unchecked")
	private static SpinnerModel createModel(ConfigAttribute attribute) {
		ValidationInfo info = attribute.getValidationInfo();
		Class<?> wrapped = wrap(attribute.getType());
		if (info instanceof NumberValidationInfo && Number.class.isAssignableFrom(wrapped)) {
			NumberValidationInfo numberValidationInfo = (NumberValidationInfo) info;
			Numbers targetType = numberType((Class<Number>) wrapped);
			Number min = targetType.convert(numberValidationInfo.min());
			Number max = targetType.convert(numberValidationInfo.max());
			Number stepSize = targetType.convert(1);
			return new SpinnerNumberModel(min, (Comparable<Number>) min, (Comparable<Number>) max, stepSize);
		}
		return new SpinnerNumberModel();
	}

	private static JComponent setState(JCheckBox checkBox, ConfigAttribute attribute) {
		checkBox.setSelected((Boolean) attribute.getValue());
		return checkBox;
	}

	private static void setSelection(JComboBox<Object> comboBox, Object value, boolean nullIsAvalidItem) {
		if (value == null) {
			if (nullIsAvalidItem) {
				comboBox.setSelectedIndex(-1);
			} else {
				selectFirstValue(comboBox);
			}
		} else {
			comboBox.setSelectedItem(value);
		}
	}

	private static void selectFirstValue(JComboBox<Object> comboBox) {
		comboBox.setSelectedIndex(comboBox.getModel().getSize() > 0 ? 0 : -1);
	}

	private static boolean isChoice(ConfigAttribute attribute) {
		return attribute.hasChoiceValues();
	}

	private static boolean isBoolean(ConfigAttribute attribute) {
		return attribute.getType().equals(Boolean.class) || attribute.getType().equals(boolean.class);
	}

	private static boolean isNumber(ConfigAttribute attribute) {
		return Number.class.isAssignableFrom(Primitives.wrap(attribute.getType()));
	}

}
