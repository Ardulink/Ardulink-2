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

import static org.ardulink.gui.facility.AbstractDocumentListenerAdapter.addDocumentListener;
import static org.ardulink.util.Primitives.tryParseAs;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
* project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ModifiableSignalButton extends JPanel {

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
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		add(tabbedPane);
		
		tabbedPane.addTab("Play", null, signalButton, null);
		
		JPanel configPanel = new JPanel();
		tabbedPane.addTab("Configure", null, configPanel, null);
		configPanel.setLayout(null);
		
		chckbxValueFieldIs = new JCheckBox("Value field is visible");
		chckbxValueFieldIs.addChangeListener(__ -> signalButton.setValueVisible(chckbxValueFieldIs.isSelected()));
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
		addDocumentListener(columnsTextField,
				__ -> tryParseAs(Integer.class, columnsTextField.getText()).ifPresent(signalButton::setValueColumns));

		JLabel lblBtnText = new JLabel("Btn. Text:");
		lblBtnText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBtnText.setBounds(6, 70, 55, 16);
		configPanel.add(lblBtnText);
		
		buttonTextField = new JTextField();
		buttonTextField.setText("Send");
		buttonTextField.setColumns(10);
		buttonTextField.setBounds(62, 64, 80, 28);
		configPanel.add(buttonTextField);
		addDocumentListener(buttonTextField, __ -> signalButton.setButtonText(buttonTextField.getText()));
		
		JLabel lblId = new JLabel("Id:");
		lblId.setHorizontalAlignment(SwingConstants.RIGHT);
		lblId.setBounds(6, 104, 55, 16);
		configPanel.add(lblId);
		
		idTextField = new JTextField();
		idTextField.setText("ID");
		idTextField.setColumns(10);
		idTextField.setBounds(62, 98, 80, 28);
		configPanel.add(idTextField);
		addDocumentListener(idTextField, __ -> signalButton.setId(idTextField.getText()));		
		
		signalButton.setValueColumns(Integer.parseInt(columnsTextField.getText()));
		signalButton.setButtonText(buttonTextField.getText());
		signalButton.setId(idTextField.getText());

	}

	public SignalButton getSignalButton() {
		return signalButton;
	}
	
	/**
	 * Set the value to be sent.
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		signalButton.setValue(value);
	}

	/**
	 * @return the value to be sent. 
	 */
	public String getValue() {
		return signalButton.getValue();
	}

	/**
	 * @return value text field visibility. 
	 */
	public boolean isValueVisible() {
		return signalButton.isValueVisible();
	}

	/**
	 * Set value text field visibility.
	 * 
	 * @param flag
	 */
	public void setValueVisible(boolean flag) {
		chckbxValueFieldIs.setSelected(flag);
	}

	/**
	 * Set value text field columns size.
	 * 
	 * @param columns
	 */
	public void setValueColumns(int columns) {
		columnsTextField.setText(String.valueOf(columns));
	}

	/**
	 * Set button's text.
	 * 
	 * @param text
	 */
	public void setButtonText(String text) {
		buttonTextField.setText(text);
	}

	/**
	 * @return id for this component.
	 */
	public String getId() {
		return signalButton.getId();
	}

	/**
	 * Set an id for this component, used in composing custom message for Arduino.
	 * 
	 * @param id
	 */
	public void setId(String id) {
		idTextField.setText(id);
	}

}
