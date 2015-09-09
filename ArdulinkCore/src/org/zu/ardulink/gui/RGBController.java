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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.zu.ardulink.Link;
import org.zu.ardulink.gui.event.PWMChangeEvent;
import org.zu.ardulink.gui.event.PWMControllerListener;
import org.zu.ardulink.gui.facility.UtilityColor;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * This class can manage three power with modulation arduino pins sending specific messages to
 * the arduino board. It is usually used to manage RGB LEDs
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class RGBController extends JPanel implements Linkable, PWMControllerListener, DocumentListener {
	
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
	private final RGBController instance = this;
	
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
		redController.addPWMControllerListener(this);
		centralPanel.add(redController);
		
		greenController = new PWMController();
		greenController.setTitle("Green");
		greenController.setPin(1);
		greenController.addPWMControllerListener(this);
		centralPanel.add(greenController);
		
		blueController = new PWMController();
		blueController.setTitle("Blue");
		blueController.setPin(0);
		blueController.addPWMControllerListener(this);
		centralPanel.add(blueController);
		
		southPanel = new JPanel();
		add(southPanel, BorderLayout.SOUTH);
		
		chckbxInverted = new JCheckBox("Inverted");
		chckbxInverted.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = coloredPanel.getBackground();
				Color newColor = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
				coloredPanel.setBackground(newColor);
				colorTextField.setText(UtilityColor.toString(newColor));
			}
		});
		southPanel.add(chckbxInverted);
		
		coloredPanel = new JPanel();
		coloredPanel.setToolTipText("click to open color dialog");
		coloredPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		coloredPanel.setPreferredSize(new Dimension(150, 40));		
		coloredPanel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				ColorChooserDialog dialog = new ColorChooserDialog(instance);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
			}
		});
		southPanel.add(coloredPanel);
		coloredPanel.setBackground(new Color(redController.getValue(), greenController.getValue(), blueController.getValue()));
		
		lblColor = new JLabel("Color:");
		southPanel.add(lblColor);
		
		colorTextField = new JTextField();
		colorTextField.setText(UtilityColor.toString(coloredPanel.getBackground()));
		southPanel.add(colorTextField);
		colorTextField.setColumns(10);
		colorTextField.getDocument().addDocumentListener(this);
	}
	
	public void setLink(Link link) {
		redController.setLink(link);
		greenController.setLink(link);
		blueController.setLink(link);
	}

	public ReplyMessageCallback getReplyMessageCallback() {
		throw new RuntimeException("Not developed yet");
	}

	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		throw new RuntimeException("Not developed yet");
	}

	@Override
	public void pwmChanged(PWMChangeEvent event) {
		colorTextField.getDocument().removeDocumentListener(this);
		
		Color color = coloredPanel.getBackground();
		int value = event.getPwmValue();
		if(chckbxInverted.isSelected()) {
			value = 255 - value;
		}
		if(event.getSource() == redController) {
			Color newColor = new Color(value, color.getGreen(), color.getBlue());
			coloredPanel.setBackground(newColor);
			colorTextField.setText(UtilityColor.toString(newColor));
		} else if(event.getSource() == greenController) {
			Color newColor = new Color(color.getRed(), value, color.getBlue());
			coloredPanel.setBackground(newColor);
			colorTextField.setText(UtilityColor.toString(newColor));
		} else if(event.getSource() == blueController) {
			Color newColor = new Color(color.getRed(), color.getGreen(), value);
			coloredPanel.setBackground(newColor);
			colorTextField.setText(UtilityColor.toString(newColor));
		}
		colorTextField.getDocument().addDocumentListener(this);
	}

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

	private void updateColor() {
		Color color = UtilityColor.toColor(colorTextField.getText());
		coloredPanel.setBackground(color);
		redController.removePWMControllerListener(this);
		greenController.removePWMControllerListener(this);
		blueController.removePWMControllerListener(this);
		if(chckbxInverted.isSelected()) {
			redController.setValue(255 - color.getRed());
			greenController.setValue(255 - color.getGreen());
			blueController.setValue(255 - color.getBlue());
		} else {
			redController.setValue(color.getRed());
			greenController.setValue(color.getGreen());
			blueController.setValue(color.getBlue());
		}
		redController.addPWMControllerListener(this);
		greenController.addPWMControllerListener(this);
		blueController.addPWMControllerListener(this);
	}
	
	public void setColor(Color color) {
		String colorString = UtilityColor.toString(color);
		colorTextField.setText(colorString);
		updateColor();
	}
	
	public Color getColor() {
		return coloredPanel.getBackground();
	}
}
