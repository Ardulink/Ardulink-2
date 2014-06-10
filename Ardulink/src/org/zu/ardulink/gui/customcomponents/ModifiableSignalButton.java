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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.zu.ardulink.Link;
import org.zu.ardulink.gui.Linkable;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ModifiableSignalButton extends JPanel implements Linkable {

	private static final long serialVersionUID = 7024281203061769142L;

	private SignalButton signalButton = new SignalButton();
	private JCheckBox chckbxValueFieldIs;
	private JTextField columnsTextField;
	private JTextField buttonTextField;
	private JTextField idTextField;

	/**
	 * Create the panel.
	 */
	public ModifiableSignalButton() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);
		
		tabbedPane.addTab("Play", null, signalButton, null);
		
		JPanel configPanel = new JPanel();
		tabbedPane.addTab("Configure", null, configPanel, null);
		configPanel.setLayout(null);
		
		chckbxValueFieldIs = new JCheckBox("Value field is visible");
		chckbxValueFieldIs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				signalButton.setValueVisible(chckbxValueFieldIs.isSelected());
			}
		});
		chckbxValueFieldIs.setSelected(true);
		chckbxValueFieldIs.setBounds(6, 6, 136, 18);
		configPanel.add(chckbxValueFieldIs);
		
		JLabel lblColumns = new JLabel("Columns:");
		lblColumns.setHorizontalAlignment(SwingConstants.RIGHT);
		lblColumns.setBounds(6, 36, 55, 16);
		configPanel.add(lblColumns);
		
		columnsTextField = new JTextField();
		columnsTextField.setText("10");
		columnsTextField.setBounds(62, 30, 80, 28);
		configPanel.add(columnsTextField);
		columnsTextField.setColumns(10);
		columnsTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateColumns();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateColumns();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateColumns();
			}
			
			private void updateColumns() {
				try {
					int columns = Integer.parseInt(columnsTextField.getText());
					signalButton.setValueColumns(columns);
				}
				catch(NumberFormatException e) {}
			}
		});
		
		JLabel lblBtnText = new JLabel("Btn. Text:");
		lblBtnText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBtnText.setBounds(6, 70, 55, 16);
		configPanel.add(lblBtnText);
		
		buttonTextField = new JTextField();
		buttonTextField.setText("Send");
		buttonTextField.setColumns(10);
		buttonTextField.setBounds(62, 64, 80, 28);
		configPanel.add(buttonTextField);
		buttonTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateButtonLabel();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateButtonLabel();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateButtonLabel();
			}
			
			private void updateButtonLabel() {
				signalButton.setButtonText(buttonTextField.getText());
			}
		});
		
		JLabel lblId = new JLabel("Id:");
		lblId.setHorizontalAlignment(SwingConstants.RIGHT);
		lblId.setBounds(6, 104, 55, 16);
		configPanel.add(lblId);
		
		idTextField = new JTextField();
		idTextField.setText("ID");
		idTextField.setColumns(10);
		idTextField.setBounds(62, 98, 80, 28);
		configPanel.add(idTextField);
		idTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateId();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateId();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateId();
			}
			
			private void updateId() {
				signalButton.setId(idTextField.getText());
			}
		});
		
		signalButton.setValueColumns(Integer.parseInt(columnsTextField.getText()));
		signalButton.setButtonText(buttonTextField.getText());
		signalButton.setId(idTextField.getText());

	}

	@Override
	public void setLink(Link link) {
		signalButton.setLink(link);
	}

	@Override
	public ReplyMessageCallback getReplyMessageCallback() {
		return signalButton.getReplyMessageCallback();
	}

	@Override
	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		signalButton.setReplyMessageCallback(replyMessageCallback);
	}

	public SignalButton getSignalButton() {
		return signalButton;
	}
	
	/**
	 * Set the value to be sent
	 * @param t
	 */
	public void setValue(String t) {
		signalButton.setValue(t);
	}

	/**
	 * @return the value to be sent
	 */
	public String getValue() {
		return signalButton.getValue();
	}

	/**
	 * @return value text field visibility
	 */
	public boolean isValueVisible() {
		return signalButton.isVisible();
	}

	/**
	 * Set value text field visibility
	 * @param aFlag
	 */
	public void setValueVisible(boolean aFlag) {
		chckbxValueFieldIs.setSelected(aFlag);
	}

	/**
	 * Set value text field columns size
	 * @param columns
	 */
	public void setValueColumns(int columns) {
		columnsTextField.setText("" + columns);
	}

	/**
	 * Set button's text
	 * @param text
	 */
	public void setButtonText(String text) {
		buttonTextField.setText(text);
	}

	/**
	 * @return id for this component
	 */
	public String getId() {
		return signalButton.getId();
	}

	/**
	 * Set an id for this component, used in composing custom message for Arduino
	 * @param id
	 */
	public void setId(String id) {
		idTextField.setText(id);
	}	
}
