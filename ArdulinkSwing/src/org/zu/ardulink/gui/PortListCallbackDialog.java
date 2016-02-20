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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class PortListCallbackDialog extends JDialog {

	private static final long serialVersionUID = -7897193872896320730L;

	private final JPanel contentPanel = new JPanel();
	private final JButton cancelButton;
	private final JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new PortListCallbackDialog().setVisible(true);
	}

	/**
	 * Create the dialog.
	 */
	public PortListCallbackDialog() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setModal(true);
		setTitle("Searching...");
		setBounds(100, 100, 335, 112);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			progressBar = new JProgressBar(0, 1);
			progressBar.setIndeterminate(true);
			contentPanel.add(progressBar);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public void stopProgressBar() {
		progressBar.setIndeterminate(false);
		progressBar.setValue(1);
	}

	public void setButtonText(String text) {
		cancelButton.setText(text);
	}

}
