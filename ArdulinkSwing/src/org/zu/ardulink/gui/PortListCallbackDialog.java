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
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.zu.ardulink.AbstractPortListCallback;
import org.zu.ardulink.PortListCallback;

public class PortListCallbackDialog extends JDialog implements PortListCallback {

	private static final long serialVersionUID = -7897193872896320730L;

	private final JPanel contentPanel = new JPanel();
	private final PortListCallbackImpl implementation;
	private final JButton cancelButton;
	private final JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		PortListCallbackDialog dialog = new PortListCallbackDialog(new JComboBox());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	/**
	 * Create the dialog.
	 */
	public PortListCallbackDialog(JComboBox comboBox) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
					public void actionPerformed(ActionEvent e) {
						implementation.setActive(false);
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		implementation = new PortListCallbackImpl(this, comboBox);
	}
	
	private static class PortListCallbackImpl extends AbstractPortListCallback {

		private final JComboBox comboBox;
		private final PortListCallbackDialog portListCallbackDialog;

		public PortListCallbackImpl(PortListCallbackDialog portListCallbackDialog, JComboBox comboBox) {
			super();
			this.comboBox = comboBox;
			this.portListCallbackDialog = portListCallbackDialog;
		}

		@Override
		public void portList(final List<String> ports) {
			// We are not on the EDT so use SwingUtilities#invokeLater 
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (ports == null || ports.isEmpty()) {
						portListCallbackDialog.setTitle("Nothing found.");
						portListCallbackDialog.setButtonText("Ok");
						portListCallbackDialog.stopProgressBar();
					} else {
						comboBox.setModel(new DefaultComboBoxModel(ports
								.toArray(new String[ports.size()])));
						portListCallbackDialog.dispose();
					}
				}
			});
		}
	}

	public boolean isActive() {
		return implementation.isActive();
	}

	public void stopProgressBar() {
		progressBar.setIndeterminate(false);
		progressBar.setValue(1);
	}

	public void setButtonText(String text) {
		cancelButton.setText(text);
	}

	public void setActive(boolean active) {
		implementation.setActive(active);
	}

	public void portList(List<String> ports) {
		implementation.portList(ports);
	}
}
