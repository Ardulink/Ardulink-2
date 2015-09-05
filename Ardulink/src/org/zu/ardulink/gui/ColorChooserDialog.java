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

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * [ardulinktitle] [ardulinkversion]
 * Used from a RGBController class to choose the right color.
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * @see RGBController
 * [adsense]
 *
 */
public class ColorChooserDialog extends JDialog {

	private static final long serialVersionUID = -9016876523594283575L;

	private final JPanel contentPanel = new JPanel();
	private final RGBController rgbController;
	private JColorChooser colorChooser;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ColorChooserDialog dialog = new ColorChooserDialog(new RGBController());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ColorChooserDialog(RGBController rgbControllerParam) {
		setModal(true);
		setTitle("Color Chooser");
		this.rgbController = rgbControllerParam;
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						rgbController.setColor(colorChooser.getColor());
						setVisible(false);
						dispose();
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		
		colorChooser = new JColorChooser();
		colorChooser.setColor(rgbController.getColor());
		contentPanel.add(colorChooser, BorderLayout.CENTER);
		this.pack();
	}

}
