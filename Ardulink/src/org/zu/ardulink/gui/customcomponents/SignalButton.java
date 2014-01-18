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

import javax.swing.JPanel;

import org.zu.ardulink.Link;
import org.zu.ardulink.gui.Linkable;
import org.zu.ardulink.protocol.ReplyMessageCallback;

import javax.swing.JButton;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.JLabel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class SignalButton extends JPanel implements Linkable {

	private static final long serialVersionUID = -5162326079507604871L;

	private Link link = Link.getDefaultInstance();
	private ReplyMessageCallback replyMessageCallback = null;

	private JTextField textField;
	private JButton signalButton;
	private JPanel valuePanel;
	private String id = "none";
	
	/**
	 * Create the panel.
	 */
	public SignalButton() {
		setLayout(new BorderLayout(0, 0));
		
		signalButton = new JButton("Send");
		signalButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = getId() + "/" + getValue();
				link.sendCustomMessage(message, replyMessageCallback);
			}
		});
		add(signalButton);
		
		valuePanel = new JPanel();
		add(valuePanel, BorderLayout.NORTH);
		
		JLabel lblValue = new JLabel("Value:");
		valuePanel.add(lblValue);
		
		textField = new JTextField();
		valuePanel.add(textField);
		textField.setColumns(20);

	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}

	/**
	 * Set the value to be sent
	 * @param t
	 */
	public void setValue(String t) {
		textField.setText(t);
	}

	/**
	 * @return the value to be sent
	 */
	public String getValue() {
		return textField.getText();
	}

	/**
	 * @return value text field visibility
	 */
	public boolean isValueVisible() {
		return valuePanel.isVisible();
	}

	/**
	 * Set value text field visibility
	 * @param aFlag
	 */
	public void setValueVisible(boolean aFlag) {
		valuePanel.setVisible(aFlag);
	}

	/**
	 * Set value text field columns size
	 * @param columns
	 */
	public void setValueColumns(int columns) {
		textField.setColumns(columns);
	}

	/**
	 * Set button's text
	 * @param text
	 */
	public void setButtonText(String text) {
		signalButton.setText(text);
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
