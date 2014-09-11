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
public class ModifiableToggleSignalButton extends JPanel implements Linkable {

	private static final long serialVersionUID = 7024281203061769142L;

	private TogggleSignalButton signalButton = new TogggleSignalButton();
	private JCheckBox chckbxValueOnFieldIs;
	private JTextField columnsTextField;
	private JTextField buttonOnTextField;
	private JCheckBox chckbxValueOffFieldIs;
	private JTextField buttonOffTextField;
	private JTextField idTextField;

	/**
	 * Create the panel.
	 */
	public ModifiableToggleSignalButton() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);
		
		tabbedPane.addTab("Play", null, signalButton, null);
		
		JPanel configPanel = new JPanel();
		tabbedPane.addTab("Configure", null, configPanel, null);
		configPanel.setLayout(null);
		
		chckbxValueOnFieldIs = new JCheckBox("Value field ON is visible");
		chckbxValueOnFieldIs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				signalButton.setValueOnVisible(chckbxValueOnFieldIs.isSelected());
			}
		});
		chckbxValueOnFieldIs.setSelected(true);
		chckbxValueOnFieldIs.setBounds(6, 8, 164, 18);
		configPanel.add(chckbxValueOnFieldIs);

		chckbxValueOffFieldIs = new JCheckBox("Value field OFF is visible");
		chckbxValueOffFieldIs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				signalButton.setValueOffVisible(chckbxValueOffFieldIs.isSelected());
			}
		});
		chckbxValueOffFieldIs.setSelected(true);
		chckbxValueOffFieldIs.setBounds(6, 34, 164, 18);
		configPanel.add(chckbxValueOffFieldIs);
		
		JLabel lblColumns = new JLabel("Columns:");
		lblColumns.setHorizontalAlignment(SwingConstants.RIGHT);
		lblColumns.setBounds(6, 66, 80, 16);
		configPanel.add(lblColumns);
		
		columnsTextField = new JTextField();
		columnsTextField.setText("10");
		columnsTextField.setBounds(90, 60, 80, 28);
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
					signalButton.setValueOnColumns(columns);
					signalButton.setValueOffColumns(columns);
				}
				catch(NumberFormatException e) {}
			}
		});
		
		JLabel lblBtnOnText = new JLabel("Btn. ON Text:");
		lblBtnOnText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBtnOnText.setBounds(6, 102, 80, 16);
		configPanel.add(lblBtnOnText);
		
		JLabel lblBtnOffText = new JLabel("Btn. OFF Text:");
		lblBtnOffText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBtnOffText.setBounds(6, 138, 80, 16);
		configPanel.add(lblBtnOffText);
		
		buttonOnTextField = new JTextField();
		buttonOnTextField.setText("Send");
		buttonOnTextField.setColumns(10);
		buttonOnTextField.setBounds(90, 96, 80, 28);
		configPanel.add(buttonOnTextField);
		buttonOnTextField.getDocument().addDocumentListener(new DocumentListener() {
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
				signalButton.setButtonTextOn(buttonOnTextField.getText());
			}
		});
		
		buttonOffTextField = new JTextField();
		buttonOffTextField.setText("Send");
		buttonOffTextField.setColumns(10);
		buttonOffTextField.setBounds(90, 132, 80, 28);
		configPanel.add(buttonOffTextField);
		buttonOffTextField.getDocument().addDocumentListener(new DocumentListener() {
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
				signalButton.setButtonTextOff(buttonOffTextField.getText());
			}
		});

		JLabel lblId = new JLabel("Id:");
		lblId.setHorizontalAlignment(SwingConstants.RIGHT);
		lblId.setBounds(6, 174, 80, 16);
		configPanel.add(lblId);
		
		idTextField = new JTextField();
		idTextField.setText("ID");
		idTextField.setColumns(10);
		idTextField.setBounds(90, 168, 80, 28);
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
		
		signalButton.setValueOnColumns(Integer.parseInt(columnsTextField.getText()));
		signalButton.setButtonTextOn(buttonOnTextField.getText());
		signalButton.setButtonTextOff(buttonOffTextField.getText());
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

	public TogggleSignalButton getSignalButton() {
		return signalButton;
	}
	
	/**
	 * Set the value ON to be sent
	 * @param t
	 */
	public void setValueOn(String t) {
		signalButton.setValueOn(t);
	}

	/**
	 * Set the value OFF to be sent
	 * @param t
	 */
	public void setValueOff(String t) {
		signalButton.setValueOff(t);
	}

	/**
	 * @return the value ON to be sent
	 */
	public String getValueOn() {
		return signalButton.getValueOn();
	}

	/**
	 * @return the value OFF to be sent
	 */
	public String getValueOff() {
		return signalButton.getValueOff();
	}
	
	/**
	 * @return value ON text field visibility
	 */
	public boolean isValueOnVisible() {
		return signalButton.isValueOnVisible();
	}

	/**
	 * @return value ON text field visibility
	 */
	public boolean isValueOffVisible() {
		return signalButton.isValueOffVisible();
	}

	/**
	 * Set value text field visibility
	 * @param aFlag
	 */
	public void setValueOnVisible(boolean aFlag) {
		chckbxValueOnFieldIs.setSelected(aFlag);
	}

	/**
	 * Set value text field visibility
	 * @param aFlag
	 */
	public void setValueOffVisible(boolean aFlag) {
		chckbxValueOffFieldIs.setSelected(aFlag);
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
	public void setButtonOnText(String text) {
		buttonOnTextField.setText(text);
	}

	/**
	 * Set button's text
	 * @param text
	 */
	public void setButtonOffText(String text) {
		buttonOffTextField.setText(text);
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
