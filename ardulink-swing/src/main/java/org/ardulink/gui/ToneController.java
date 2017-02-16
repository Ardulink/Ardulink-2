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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.ardulink.gui.facility.IntMinMaxModel;
import org.ardulink.legacy.Link;

public class ToneController extends JPanel implements Linkable {

	private static final long serialVersionUID = 1754259889096759199L;

	private Link link;

	private JSpinner frequencySpinner;
	private JToggleButton toneButton;
	private JPanel frequencyPanel;
	private JLabel frequencyLabel;
	private JPanel configPanel;
	private JPanel durationPanel;
	private JLabel durationLabel;
	private JSpinner durationSpinner;
	
	private String toneButtonOnText = "On";
	private String toneButtonOffText = "Off";
	private JCheckBox durationCheckBox;
	private IntMinMaxModel pinComboBoxModel;
	private JLabel pinLabel;
	private JPanel pinPanel;

	
	/**
	 * Create the valuePanelOff.
	 */
	public ToneController() {
		setLayout(new BorderLayout(0, 0));
		
		configPanel = new JPanel();
		add(configPanel, BorderLayout.NORTH);
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
		
		pinPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pinPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		configPanel.add(pinPanel);
		
		pinLabel = new JLabel("Pin:");
		pinPanel.add(pinLabel);
		pinLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		
		pinComboBoxModel = new IntMinMaxModel(0, 40);
		JComboBox pinComboBox = new JComboBox(pinComboBoxModel);
		pinPanel.add(pinComboBox);
		
		frequencyPanel = new JPanel();
		configPanel.add(frequencyPanel);
		frequencyPanel.setLayout(new BoxLayout(frequencyPanel, BoxLayout.X_AXIS));
		
		frequencyLabel = new JLabel("Frequency (Hz):");
		frequencyLabel.setToolTipText("the frequency of the tone in hertz");
		frequencyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		frequencyPanel.add(frequencyLabel);
		
		frequencySpinner = new JSpinner();
		frequencySpinner.setModel(new SpinnerNumberModel(31, 1, 65535, 1));
		frequencyPanel.add(frequencySpinner);

		durationPanel = new JPanel();
		configPanel.add(durationPanel);
		durationPanel.setLayout(new BoxLayout(durationPanel, BoxLayout.X_AXIS));
		
		durationLabel = new JLabel("Duration (ms):");
		durationLabel.setToolTipText("the duration of the tone in milliseconds (optional)");
		durationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		durationPanel.add(durationLabel);
		
		durationSpinner = new JSpinner();
		durationSpinner.setPreferredSize(new Dimension(105, 28));
		durationSpinner.setModel(new SpinnerNumberModel(new Integer(1000), new Integer(1), null, new Integer(1)));
		durationPanel.add(durationSpinner);
		
		durationCheckBox = new JCheckBox("Duration enabled");
		durationCheckBox.setSelected(true);
		durationCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(durationCheckBox.isSelected()) {
					durationLabel.setEnabled(true);
					durationSpinner.setEnabled(true);
				} else {
					durationLabel.setEnabled(false);
					durationSpinner.setEnabled(false);
				}
			}
		});
		durationPanel.add(durationCheckBox);
		
		
		toneButton = new JToggleButton("Off");
		toneButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Integer pin = pinComboBoxModel.getSelectedItem();
				if(pin != null) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						Integer value = (Integer) frequencySpinner.getValue();
						if (durationCheckBox.isSelected()) {
							link.sendToneMessage(pin, value,
									(Integer) durationSpinner.getValue());
						} else {
							link.sendToneMessage(pin, value);
						}
						updateToneButtonText();
					} else if(e.getStateChange() == ItemEvent.DESELECTED) {
						link.sendNoToneMessage(pin);
						updateToneButtonText();
					}
				}
			}
		});
		
		add(toneButton);

	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}

	/**
	 * @return the string valueLabelOn for value parameter
	 */
	public String getValueLabelOn() {
		return frequencyLabel.getText();
	}

	/**
	 * Set the string valueLabelOn for value parameter
	 * @param valueLabelOn
	 */
	public void setValueLabelOn(String valueLabel) {
		this.frequencyLabel.setText(valueLabel);
	}

	/**
	 * @return the string valueLabelOff for value parameter
	 */
	public String getValueLabelOff() {
		return durationLabel.getText();
	}

	/**
	 * Set the string valueLabelOff for value parameter
	 * @param valueLabelOn
	 */
	public void setValueLabelOff(String valueLabel) {
		this.durationLabel.setText(valueLabel);
	}

	/**
	 * Set the frequency to be sent
	 * @param t
	 */
	public void setFrequency(int frequency) {
		frequencySpinner.setValue(frequency);;
	}

	/**
	 * @return the frequency to be sent
	 */
	public int getFrequency() {
		return (Integer)frequencySpinner.getValue();
	}

	/**
	 * Set the duration to be sent
	 * @param t
	 */
	public void setDuration(int duration) {
		durationSpinner.setValue(duration);
	}

	/**
	 * @return the duration to be sent
	 */
	public int getDuration() {
		return (Integer)durationSpinner.getValue();
	}

	/**
	 * @return frequency visibility
	 */
	public boolean isFrequencyVisible() {
		return frequencyPanel.isVisible();
	}

	/**
	 * Set frequency visibility
	 * @param aFlag
	 */
	public void setFrequencyVisible(boolean aFlag) {
		frequencyPanel.setVisible(aFlag);
	}

	/**
	 * @return duration visibility
	 */
	public boolean isDurationVisible() {
		return durationPanel.isVisible();
	}

	/**
	 * Set duration visibility
	 * @param aFlag
	 */
	public void setDurationVisible(boolean aFlag) {
		durationPanel.setVisible(aFlag);
	}


	/**
	 * Set button's text (ON state)
	 * @param text
	 */
	public void setButtonTextOn(String text) {
		toneButtonOnText = text;
		updateToneButtonText();
	}

	/**
	 * Set button's text (OFF state)
	 * @param text
	 */
	public void setButtonTextOff(String text) {
		toneButtonOffText = text;
		updateToneButtonText();
	}

	private void updateToneButtonText() {
		if(toneButton.isSelected()) {
			toneButton.setText(toneButtonOnText);
		} else {
			toneButton.setText(toneButtonOffText);
		}
	}

	public void setIcon(Icon defaultIcon) {
		toneButton.setIcon(defaultIcon);
	}

	public void setPressedIcon(Icon pressedIcon) {
		toneButton.setPressedIcon(pressedIcon);
	}

	public void setSelectedIcon(Icon selectedIcon) {
		toneButton.setSelectedIcon(selectedIcon);
	}

	public void setRolloverIcon(Icon rolloverIcon) {
		toneButton.setRolloverIcon(rolloverIcon);
	}

	public void setRolloverSelectedIcon(Icon rolloverSelectedIcon) {
		toneButton.setRolloverSelectedIcon(rolloverSelectedIcon);
	}

	public void setDisabledIcon(Icon disabledIcon) {
		toneButton.setDisabledIcon(disabledIcon);
	}

	public void setDisabledSelectedIcon(Icon disabledSelectedIcon) {
		toneButton.setDisabledSelectedIcon(disabledSelectedIcon);
	}

	public void setIconTextGap(int iconTextGap) {
		toneButton.setIconTextGap(iconTextGap);
	}

	public void setRolloverEnabled(boolean b) {
		toneButton.setRolloverEnabled(b);
	}

	@Override
	public void setForeground(Color fg) {
		if(toneButton != null) {
			toneButton.setForeground(fg);
		}
	}

	@Override
	public void setBackground(Color bg) {
		if(toneButton != null) {
			toneButton.setBackground(bg);
		}
	}

}
