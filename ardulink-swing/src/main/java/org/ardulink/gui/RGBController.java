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
import static org.ardulink.gui.facility.Colors.invert;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiFunction;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.ardulink.gui.event.PWMChangeEvent;
import org.ardulink.gui.event.PWMControllerListener;
import org.ardulink.gui.facility.Colors;
import org.ardulink.legacy.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * This class can manage three power with modulation arduino pins sending
 * specific messages to the arduino board. It is usually used to manage RGB LEDs. 
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class RGBController extends JPanel implements Linkable {

	private final transient DefaultDocumentListener colorTextFieldDocumentListener = new DefaultDocumentListener();

	private final class DefaultDocumentListener implements DocumentListener {
		
		private boolean enabled;

		private void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public void removeUpdate(DocumentEvent documentEvent) {
			doUpdate();
		}

		@Override
		public void insertUpdate(DocumentEvent documentEvent) {
			doUpdate();
		}

		@Override
		public void changedUpdate(DocumentEvent documentEvent) {
			doUpdate();
		}

		private void doUpdate() {
			if (!enabled) {
				return;
			}
			updateColor();
		}
	}

	private class DefaultPWMControllerListener implements PWMControllerListener {
		
		private boolean enabled = true;
		
		private final BiFunction<Color, Integer, Color> colorMaker;

		private DefaultPWMControllerListener(BiFunction<Color, Integer, Color> colorMaker) {
			this.colorMaker = colorMaker;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public void pwmChanged(PWMChangeEvent event) {
			if (!enabled) {
				return;
			}
			colorTextFieldDocumentListener.setEnabled(false);
			try {
				int value = event.getPwmValue();
				Color color = colorMaker.apply(coloredPanel.getBackground(),
						chckbxInverted.isSelected() ? invert(value) : value);
				coloredPanel.setBackground(color);
				colorTextField.setText(Colors.toString(color));
			} finally {
				colorTextFieldDocumentListener.setEnabled(true);
			}
		}

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

	private final transient DefaultPWMControllerListener redListener = new DefaultPWMControllerListener(
			(c, v) -> new Color(v, c.getGreen(), c.getBlue()));

	private final transient DefaultPWMControllerListener greenListener = new DefaultPWMControllerListener(
			(c, v) -> new Color(c.getRed(), v, c.getBlue()));

	private final transient DefaultPWMControllerListener blueListener = new DefaultPWMControllerListener(
			(c, v) -> new Color(c.getRed(), c.getGreen(), v));

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
		
		chckbxInverted = new JCheckBox("Inverted");
		chckbxInverted.addActionListener(__ -> {
			Color newColor = Colors.invert(coloredPanel.getBackground());
			coloredPanel.setBackground(newColor);
			colorTextField.setText(Colors.toString(newColor));
		});
		southPanel.add(chckbxInverted);

		southPanel.add(coloredPanel);
		coloredPanel.setBackground(new Color(redController.getValue(),
				greenController.getValue(), blueController.getValue()));

		lblColor = new JLabel("Color:");
		southPanel.add(lblColor);

		colorTextField = new JTextField();
		colorTextField.setText(Colors.toString(coloredPanel
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
		Color color = Colors.toColor(colorTextField.getText());
		coloredPanel.setBackground(color);
		enableListeners(false);
		try {
			if (chckbxInverted.isSelected()) {
				color = Colors.invert(color);
			}
			redController.setValue(color.getRed());
			greenController.setValue(color.getGreen());
			blueController.setValue(color.getBlue());
		} finally {
			enableListeners(true);
		}
	}

	private void enableListeners(boolean state) {
		redListener.setEnabled(state);
		greenListener.setEnabled(state);
		blueListener.setEnabled(state);
	}

	public void setColor(Color color) {
		colorTextField.setText(Colors.toString(color));
		updateColor();
	}

}
