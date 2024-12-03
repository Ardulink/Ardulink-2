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

package org.ardulink.gui.customcomponents;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ardulink.gui.Linkable;
import org.ardulink.legacy.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class SignalButton extends JPanel implements Linkable {

	private static final long serialVersionUID = -5162326079507604871L;

	private transient Link link;

	private JTextField textField;
	private JButton signalButton;
	private JPanel valuePanel;
	private String id = "none";
	private JLabel valueLabel;
	
	/**
	 * Create the panel.
	 */
	public SignalButton() {
		setLayout(new BorderLayout(0, 0));
		
		signalButton = new JButton("Send");
		signalButton.addActionListener(__ -> link.sendCustomMessage(getId(), getValue()));
		add(signalButton);
		
		valuePanel = new JPanel();
		add(valuePanel, BorderLayout.NORTH);
		
		valueLabel = new JLabel("Value:");
		valuePanel.add(valueLabel);
		
		textField = new JTextField();
		valuePanel.add(textField);
		textField.setColumns(10);
		textField.setMinimumSize(getPreferredSize());

	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}

	/**
	 * @return the string label for value parameter. 
	 */
	public String getValueLabel() {
		return valueLabel.getText();
	}

	/**
	 * Set the string label for value parameter.
	 * 
	 * @param label
	 */
	public void setValueLabel(String label) {
		this.valueLabel.setText(label);
	}

	/**
	 * Set the value to be sent.
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		textField.setText(value);
	}

	/**
	 * @return the value to be sent.
	 */
	public String getValue() {
		return textField.getText();
	}

	/**
	 * @return value text field visibility.
	 */
	public boolean isValueVisible() {
		return valuePanel.isVisible();
	}

	/**
	 * Set value text field visibility.
	 * 
	 * @param flag
	 */
	public void setValueVisible(boolean flag) {
		valuePanel.setVisible(flag);
	}

	/**
	 * Set value text field columns size.
	 * 
	 * @param columns
	 */
	public void setValueColumns(int columns) {
		textField.setColumns(columns);
	}

	/**
	 * Set button's text.
	 * 
	 * @param text
	 */
	public void setButtonText(String text) {
		signalButton.setText(text);
	}

	/**
	 * @return id for this component. 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set an id for this component, used in composing custom message for Arduino.
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	public void setIcon(Icon defaultIcon) {
		signalButton.setIcon(defaultIcon);
	}

	public void setPressedIcon(Icon pressedIcon) {
		signalButton.setPressedIcon(pressedIcon);
	}

	public void setSelectedIcon(Icon selectedIcon) {
		signalButton.setSelectedIcon(selectedIcon);
	}

	public void setRolloverIcon(Icon rolloverIcon) {
		signalButton.setRolloverIcon(rolloverIcon);
	}

	public void setRolloverSelectedIcon(Icon rolloverSelectedIcon) {
		signalButton.setRolloverSelectedIcon(rolloverSelectedIcon);
	}

	public void setDisabledIcon(Icon disabledIcon) {
		signalButton.setDisabledIcon(disabledIcon);
	}

	public void setDisabledSelectedIcon(Icon disabledSelectedIcon) {
		signalButton.setDisabledSelectedIcon(disabledSelectedIcon);
	}

	public void setIconTextGap(int iconTextGap) {
		signalButton.setIconTextGap(iconTextGap);
	}

	public void setRolloverEnabled(boolean rollover) {
		signalButton.setRolloverEnabled(rollover);
	}

	@Override
	public void setForeground(Color color) {
		if (signalButton != null) {
			signalButton.setForeground(color);
		}
	}

	@Override
	public void setBackground(Color color) {
		if (signalButton != null) {
			signalButton.setBackground(color);
		}
	}
	
}
