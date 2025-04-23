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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.Pin.analogPin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

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

import org.ardulink.core.Link;
import org.ardulink.core.Tone;
import org.ardulink.core.Tone.Builder;
import org.ardulink.gui.facility.IntMinMaxModel;
import org.ardulink.util.Throwables;

public class ToneController extends JPanel implements Linkable {

	private static final long serialVersionUID = 1754259889096759199L;

	private static final String TONE_BUTTON_ON_TEXT = "On";
	private static final String TONE_BUTTON_OFF_TEXT = "Off";

	private transient Link link;

	private final JSpinner frequencySpinner;
	private final JToggleButton toneButton;
	private final JPanel frequencyPanel;
	private final JLabel frequencyLabel;
	private final JPanel configPanel;
	private final JPanel durationPanel;
	private final JLabel durationLabel;
	private final JSpinner durationSpinner;

	private final JCheckBox durationCheckBox;
	private final IntMinMaxModel pinComboBoxModel;
	private final JLabel pinLabel;
	private final JPanel pinPanel;

	/**
	 * Create the ToneController.
	 */
	public ToneController() {
		setLayout(new BorderLayout());

		configPanel = new JPanel();
		add(configPanel, BorderLayout.NORTH);
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));

		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		pinPanel = new JPanel(flowLayout);
		configPanel.add(pinPanel);

		pinLabel = new JLabel("Pin:");
		pinPanel.add(pinLabel);
		pinLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

		pinComboBoxModel = new IntMinMaxModel(0, 40);
		pinPanel.add(new JComboBox<>(pinComboBoxModel));

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
		durationSpinner
				.setModel(new SpinnerNumberModel(Integer.valueOf(1000), Integer.valueOf(1), null, Integer.valueOf(1)));
		durationPanel.add(durationSpinner);

		durationCheckBox = new JCheckBox("Duration enabled");
		durationCheckBox.setSelected(true);
		durationCheckBox.addActionListener(__ -> {
			boolean isCbSelected = durationCheckBox.isSelected();
			durationLabel.setEnabled(isCbSelected);
			durationSpinner.setEnabled(isCbSelected);
		});
		durationPanel.add(durationCheckBox);

		toneButton = new JToggleButton(TONE_BUTTON_OFF_TEXT);
		toneButton.addItemListener(itemListener());
		add(toneButton);
	}

	private ItemListener itemListener() {
		return e -> {
			Integer pin = pinComboBoxModel.getSelectedItem();
			if (pin != null) {
				try {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Builder tone = Tone.forPin(analogPin(pin)).withHertz((int) frequencySpinner.getValue());
						link.sendTone(durationCheckBox.isSelected() //
								? tone.withDuration((int) durationSpinner.getValue(), MILLISECONDS) //
								: tone.endless());
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						link.sendNoTone(analogPin(pin));
					}
					toneButton.setText(toneButton.isSelected() ? TONE_BUTTON_ON_TEXT : TONE_BUTTON_OFF_TEXT);
				} catch (IOException ex) {
					throw Throwables.propagate(ex);
				}
			}
		};
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
	 * 
	 * @param valueLabel
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
	 * 
	 * @param valueLabel
	 */
	public void setValueLabelOff(String valueLabel) {
		this.durationLabel.setText(valueLabel);
	}

	/**
	 * Set the frequency to be sent
	 * 
	 * @param frequency
	 */
	public void setFrequency(int frequency) {
		frequencySpinner.setValue(frequency);
	}

	/**
	 * @return the frequency to be sent
	 */
	public int getFrequency() {
		return (Integer) frequencySpinner.getValue();
	}

	/**
	 * Set the duration to be sent
	 * 
	 * @param duration
	 */
	public void setDuration(int duration) {
		durationSpinner.setValue(duration);
	}

	/**
	 * @return the duration to be sent
	 */
	public int getDuration() {
		return (Integer) durationSpinner.getValue();
	}

	/**
	 * @return frequency visibility
	 */
	public boolean isFrequencyVisible() {
		return frequencyPanel.isVisible();
	}

	/**
	 * Set frequency visibility
	 * 
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
	 * 
	 * @param visible
	 */
	public void setDurationVisible(boolean visible) {
		durationPanel.setVisible(visible);
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

	public void setRolloverEnabled(boolean enabled) {
		toneButton.setRolloverEnabled(enabled);
	}

	@Override
	public void setForeground(Color fg) {
		if (toneButton != null) {
			toneButton.setForeground(fg);
		}
	}

	@Override
	public void setBackground(Color bg) {
		if (toneButton != null) {
			toneButton.setBackground(bg);
		}
	}

}
