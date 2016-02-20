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

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.zu.ardulink.gui.facility.UtilityGeometry;
import org.zu.ardulink.legacy.Link;

/**
 * [ardulinktitle] [ardulinkversion] This component is Able to search for
 * bluetooth devices able to use Ardulink and select one.
 *
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 *         [adsense]
 *
 */
public class BluetoothConnectionPanel extends JPanel implements Linkable {

	private static final long serialVersionUID = -3658770765086157064L;
	private final JComboBox deviceComboBox;
	private final JButton discoverButton;

	private Link link = Link.createInstance("bluetooth");

	/**
	 * Create the panel.
	 */
	public BluetoothConnectionPanel() {

		Dimension dimension = new Dimension(275, 55);
		setPreferredSize(dimension);
		setMinimumSize(dimension);
		setLayout(null);

		JLabel devicesLabel = new JLabel("Devices:");
		devicesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		devicesLabel.setBounds(6, 17, 65, 16);
		add(devicesLabel);

		deviceComboBox = new JComboBox();
		deviceComboBox.setBounds(67, 12, 165, 26);
		add(deviceComboBox);

		discoverButton = new JButton("");
		discoverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deviceComboBox.removeAllItems();
				PortListCallbackDialog dialog = new PortListCallbackDialog();
				UtilityGeometry.setAlignmentCentered(dialog,
						SwingUtilities.getRoot((Component) e.getSource()));
				getAsynchPortList(dialog, deviceComboBox);
				dialog.setVisible(true);
			}
		});
		discoverButton.setIcon(new ImageIcon(BluetoothConnectionPanel.class
				.getResource("icons/search_icon.png")));
		discoverButton.setToolTipText("Discover");
		discoverButton.setBounds(237, 9, 32, 32);
		add(discoverButton);

	}

	/**
	 * This method is used to retrieve asynchronously the port list. It is used
	 * when the operation take long time.
	 * 
	 * @return ports available in a async way.
	 */
	private void getAsynchPortList(final PortListCallbackDialog dialog,
			final JComboBox comboBox) {
		newSingleThreadExecutor().submit(new Callable<Void>() {
			@Override
			public Void call() {
				final String[] ports = (String[]) link.getChoiceValues("portlist");
				// We are not on the EDT so use SwingUtilities#invokeLater
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// TODO Do we really need to check #isDisplayable?
						if (dialog.isDisplayable()) {
							DefaultComboBoxModel model = new DefaultComboBoxModel(
									ports == null || ports.length == 0 ? new String[0]
											: ports);
							comboBox.setModel(model);
							if (model.getSize() == 0) {
								dialog.setTitle("Nothing found.");
								dialog.setButtonText("Ok");
								dialog.stopProgressBar();
							} else {
								dialog.dispose();
							}
						}
					}
				});
				return null;
			}
		});
	}

	/**
	 * @return the connection port name selected or set
	 */
	public String getConnectionPort() {
		return deviceComboBox.getSelectedItem().toString();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		deviceComboBox.setEnabled(enabled);
		discoverButton.setEnabled(enabled);
	}

	public Link getLink() {
		return link;
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}

	public String getSelectedDevice() {
		return (String) deviceComboBox.getSelectedItem();
	}
}
