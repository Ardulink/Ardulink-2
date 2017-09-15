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

package org.ardulink.gui;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.ardulink.gui.event.PWMChangeEvent;
import org.ardulink.gui.event.PWMControllerListener;
import org.ardulink.gui.facility.UtilityColor;
import org.ardulink.legacy.Link;

/**
 * [ardulinktitle] [ardulinkversion] This class can manage three power with
 * modulation arduino pins sending specific messages to the arduino board. It is
 * usually used to manage RGB LEDs project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class RGBController extends JPanel implements Linkable {

	private final DocumentListener colorTextFieldDocumentListener = new DocumentListener() {

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateColor();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateColor();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateColor();
		}

	};

	public abstract class AbstractPWMControllerListener implements
			PWMControllerListener {

		@Override
		public void pwmChanged(PWMChangeEvent event) {
			colorTextField.getDocument().removeDocumentListener(
					colorTextFieldDocumentListener);

			Color color = coloredPanel.getBackground();
			int value = event.getPwmValue();
			if (chckbxInverted.isSelected()) {
				value = 255 - value;
			}
			Color newColor = makeColor(color, value);
			coloredPanel.setBackground(newColor);
			colorTextField.setText(UtilityColor.toString(newColor));
			colorTextField.getDocument().addDocumentListener(
					colorTextFieldDocumentListener);
		}

		protected abstract Color makeColor(Color color, int value);

	}

	private static final long serialVersionUID = -4822268873266363743L;

	private PWMController redController;
	private PWMController greenController;
	private PWMController blueController;

	private JPanel centralPanel;
	private final JPanel coloredPanel;
	private JPanel southPanel;
	private JLabel lblColor;
	private JTextField colorTextField;
	private JCheckBox chckbxInverted;

	private final PWMControllerListener redListener = new AbstractPWMControllerListener() {

		@Override
		protected Color makeColor(Color color, int value) {
			return new Color(value, color.getGreen(), color.getBlue());
		}

	};

	private final AbstractPWMControllerListener greenListener = new AbstractPWMControllerListener() {

		@Override
		protected Color makeColor(Color color, int value) {
			return new Color(color.getRed(), value, color.getBlue());
		}

	};

	private final AbstractPWMControllerListener blueListener = new AbstractPWMControllerListener() {

		@Override
		protected Color makeColor(Color color, int value) {
			return new Color(color.getRed(), color.getGreen(), value);
		}

	};

	/**
	 * Create the panel.
	 */
	public RGBController() {
		setPreferredSize(new Dimension(640, 315));
		setLayout(new BorderLayout(0, 0));

		centralPanel = new JPanel();
		add(centralPanel, BorderLayout.CENTER);

		redController = new PWMController();
		redController.setTitle("Red");
		redController.setPin(2);
		redController.addPWMControllerListener(redListener);
		centralPanel.add(redController);

		greenController = new PWMController();
		greenController.setTitle("Green");
		greenController.setPin(1);
		greenController.addPWMControllerListener(greenListener);
		centralPanel.add(greenController);

		blueController = new PWMController();
		blueController.setTitle("Blue");
		blueController.setPin(0);
		blueController.addPWMControllerListener(blueListener);
		centralPanel.add(blueController);

		southPanel = new JPanel();
		add(southPanel, BorderLayout.SOUTH);

		chckbxInverted = new JCheckBox("Inverted");
		chckbxInverted.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Color newColor = invert(coloredPanel.getBackground());
				coloredPanel.setBackground(newColor);
				colorTextField.setText(UtilityColor.toString(newColor));
			}

		});
		southPanel.add(chckbxInverted);

		coloredPanel = new JPanel();
		coloredPanel.setToolTipText("click to open color dialog");
		coloredPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));
		coloredPanel.setPreferredSize(new Dimension(150, 40));
		coloredPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ColorChooserDialog dialog = new ColorChooserDialog(coloredPanel
						.getBackground());
				dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
				setColor(dialog.getColor());
			}
		});
		southPanel.add(coloredPanel);
		coloredPanel.setBackground(new Color(redController.getValue(),
				greenController.getValue(), blueController.getValue()));

		lblColor = new JLabel("Color:");
		southPanel.add(lblColor);

		colorTextField = new JTextField();
		colorTextField.setText(UtilityColor.toString(coloredPanel
				.getBackground()));
		southPanel.add(colorTextField);
		colorTextField.setColumns(10);
		colorTextField.getDocument().addDocumentListener(
				colorTextFieldDocumentListener);
	}

	@Override
	public void setLink(Link link) {
		redController.setLink(link);
		greenController.setLink(link);
		blueController.setLink(link);
	}

	private void updateColor() {
		Color color = UtilityColor.toColor(colorTextField.getText());
		coloredPanel.setBackground(color);
		redController.removePWMControllerListener(redListener);
		greenController.removePWMControllerListener(greenListener);
		blueController.removePWMControllerListener(blueListener);
		if (chckbxInverted.isSelected()) {
			color = invert(color);
		}
		redController.setValue(color.getRed());
		greenController.setValue(color.getGreen());
		blueController.setValue(color.getBlue());
		redController.addPWMControllerListener(redListener);
		greenController.addPWMControllerListener(greenListener);
		blueController.addPWMControllerListener(blueListener);
	}

	private Color invert(Color color) {
		return new Color(255 - color.getRed(), 255 - color.getGreen(),
				255 - color.getBlue());
	}

	public void setColor(Color color) {
		String colorString = UtilityColor.toString(color);
		colorTextField.setText(colorString);
		updateColor();
	}

}
