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

package org.zu.ardulink.gui.customcomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.zu.ardulink.Link;
import org.zu.ardulink.gui.Linkable;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class TogggleSignalButton extends JPanel implements Linkable {

	private static final long serialVersionUID = -5162326079507604871L;

	private Link link = Link.getDefaultInstance();
	private ReplyMessageCallback replyMessageCallback = null;

	private JTextField textFieldOn;
	private JToggleButton signalButton;
	private JPanel valuePanelOn;
	private String id = "none";
	private JLabel valueLabelOn;
	private JPanel configPanel;
	private JPanel valuePanelOff;
	private JLabel valueLabelOff;
	private JTextField textFieldOff;
	
	private String signalButtonOnText = "On";
	private String signalButtonOffText = "Off";
	
	/**
	 * Create the valuePanelOff.
	 */
	public TogggleSignalButton() {
		setLayout(new BorderLayout(0, 0));
		
		configPanel = new JPanel();
		add(configPanel, BorderLayout.NORTH);
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
		
		valuePanelOn = new JPanel();
		configPanel.add(valuePanelOn);
		valuePanelOn.setLayout(new BoxLayout(valuePanelOn, BoxLayout.X_AXIS));
		
		valueLabelOn = new JLabel("Value on:");
		valueLabelOn.setHorizontalAlignment(SwingConstants.RIGHT);
		valuePanelOn.add(valueLabelOn);
		
		textFieldOn = new JTextField();
		valuePanelOn.add(textFieldOn);
		textFieldOn.setColumns(10);

		valuePanelOff = new JPanel();
		configPanel.add(valuePanelOff);
		valuePanelOff.setLayout(new BoxLayout(valuePanelOff, BoxLayout.X_AXIS));
		
		valueLabelOff = new JLabel("Value off:");
		valueLabelOff.setHorizontalAlignment(SwingConstants.RIGHT);
		valuePanelOff.add(valueLabelOff);
		
		textFieldOff = new JTextField();
		textFieldOff.setColumns(10);
		valuePanelOff.add(textFieldOff);
		
		
		signalButton = new JToggleButton("Off");
		signalButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					String message = getId() + "/" + getValueOn();
					link.sendCustomMessage(message, replyMessageCallback);
					
					updateSignalButtonText();
				} else if(e.getStateChange() == ItemEvent.DESELECTED) {
					String message = getId() + "/" + getValueOff();
					link.sendCustomMessage(message, replyMessageCallback);

					updateSignalButtonText();
				}
			}
		});
		
		add(signalButton);

	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}

	/**
	 * @return the string valueLabelOn for value parameter
	 */
	public String getValueLabelOn() {
		return valueLabelOn.getText();
	}

	/**
	 * Set the string valueLabelOn for value parameter
	 * @param valueLabelOn
	 */
	public void setValueLabelOn(String valueLabel) {
		this.valueLabelOn.setText(valueLabel);
	}

	/**
	 * @return the string valueLabelOff for value parameter
	 */
	public String getValueLabelOff() {
		return valueLabelOff.getText();
	}

	/**
	 * Set the string valueLabelOff for value parameter
	 * @param valueLabelOn
	 */
	public void setValueLabelOff(String valueLabel) {
		this.valueLabelOff.setText(valueLabel);
	}

	/**
	 * Set the value ON to be sent
	 * @param t
	 */
	public void setValueOn(String t) {
		textFieldOn.setText(t);
	}

	/**
	 * @return the value ON to be sent
	 */
	public String getValueOn() {
		return textFieldOn.getText();
	}

	/**
	 * Set the value OFF to be sent
	 * @param t
	 */
	public void setValueOff(String t) {
		textFieldOff.setText(t);
	}

	/**
	 * @return the value OFF to be sent
	 */
	public String getValueOff() {
		return textFieldOff.getText();
	}

	/**
	 * @return value text field visibility
	 */
	public boolean isValueOnVisible() {
		return valuePanelOn.isVisible();
	}

	/**
	 * Set value text field visibility
	 * @param aFlag
	 */
	public void setValueOnVisible(boolean aFlag) {
		valuePanelOn.setVisible(aFlag);
	}

	/**
	 * @return value text field visibility
	 */
	public boolean isValueOffVisible() {
		return valuePanelOff.isVisible();
	}

	/**
	 * Set value text field visibility
	 * @param aFlag
	 */
	public void setValueOffVisible(boolean aFlag) {
		valuePanelOff.setVisible(aFlag);
	}

	/**
	 * Set value text field ON columns size
	 * @param columns
	 */
	public void setValueOnColumns(int columns) {
		textFieldOn.setColumns(columns);
	}

	/**
	 * Set value text field OFF columns size
	 * @param columns
	 */
	public void setValueOffColumns(int columns) {
		textFieldOff.setColumns(columns);
	}

	/**
	 * Set button's text (ON state)
	 * @param text
	 */
	public void setButtonTextOn(String text) {
		signalButtonOnText = text;
		updateSignalButtonText();
	}

	/**
	 * Set button's text (OFF state)
	 * @param text
	 */
	public void setButtonTextOff(String text) {
		signalButtonOffText = text;
		updateSignalButtonText();
	}

	private void updateSignalButtonText() {
		if(signalButton.isSelected()) {
			signalButton.setText(signalButtonOnText);
		} else {
			signalButton.setText(signalButtonOffText);
		}
	}

	/**
	 * @return id for this component
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set an id for this component, used in composing custom message for Arduino
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	public ReplyMessageCallback getReplyMessageCallback() {
		return replyMessageCallback;
	}

	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		this.replyMessageCallback = replyMessageCallback;
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

	public void setRolloverEnabled(boolean b) {
		signalButton.setRolloverEnabled(b);
	}

	public void setForeground(Color fg) {
		if(signalButton != null) {
			signalButton.setForeground(fg);
		}
	}

	public void setBackground(Color bg) {
		if(signalButton != null) {
			signalButton.setBackground(bg);
		}
	}
}
