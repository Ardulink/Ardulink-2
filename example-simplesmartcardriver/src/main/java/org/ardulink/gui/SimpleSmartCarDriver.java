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
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;

import org.ardulink.core.ConnectionListener;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.gui.customcomponents.SignalButton;
import org.ardulink.legacy.Link;
import org.ardulink.util.Lists;
import org.ardulink.util.URIs;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class SimpleSmartCarDriver extends JFrame implements Linkable {

	private static final long serialVersionUID = 6065022178316507177L;

	private JPanel contentPane;
	private Link link;
	private List<Linkable> linkables = Lists.newArrayList();

	private BluetoothConnectionPanel bluetoothConnectionPanel;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private JPanel controlPanel;
	private SignalButton btnAhead;
	private SignalButton btnLeft;
	private SignalButton btnRight;
	private SignalButton btnBack;

	private static final String ICON_FOLDER = "icons/";
	private static final ImageIcon AHEAD_ICON = loadIcon("arrow-up.png");
	private static final ImageIcon LEFT_ICON = loadIcon("arrow-left.png");
	private static final ImageIcon RIGHT_ICON = loadIcon("arrow-right.png");
	private static final ImageIcon BACK_ICON = loadIcon("arrow-down.png");

	private final ConnectionListener connectionListener = new ConnectionListener() {

		@Override
		public void reconnected() {
			bluetoothConnectionPanel.setEnabled(false);
			btnConnect.setEnabled(false);
			btnDisconnect.setEnabled(true);
		}

		@Override
		public void connectionLost() {
			bluetoothConnectionPanel.setEnabled(true);
			btnConnect.setEnabled(true);
			btnDisconnect.setEnabled(false);
		}

	};

	private static ImageIcon loadIcon(String iconName) {
		return new ImageIcon(SimpleSmartCarDriver.class.getResource(ICON_FOLDER
				+ iconName));
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					for (LookAndFeelInfo laf : UIManager
							.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(laf.getName())) {
							UIManager.setLookAndFeel(laf.getClassName());
						}
					}
					SimpleSmartCarDriver frame = new SimpleSmartCarDriver();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SimpleSmartCarDriver() {
		setTitle("Simple Smart Car Driver");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 735, 407);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel connectionPanel = new JPanel();
		contentPane.add(connectionPanel, BorderLayout.SOUTH);

		bluetoothConnectionPanel = new BluetoothConnectionPanel();
		connectionPanel.add(bluetoothConnectionPanel);

		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (link != null) {
					Configurer configurer = LinkManager.getInstance()
							.getConfigurer(URIs.newURI("ardulink://bluetooth"));
					configurer.getAttribute("deviceName").setValue(
							bluetoothConnectionPanel.getSelectedDevice());
					link = new Link.LegacyLinkAdapter(Links.getLink(configurer));
				}
			}
		});
		connectionPanel.add(btnConnect);

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (link != null) {
					link.disconnect();
				}
			}
		});
		btnDisconnect.setEnabled(false);
		connectionPanel.add(btnDisconnect);

		ConnectionStatus connectionStatus = new ConnectionStatus();
		connectionPanel.add(connectionStatus);
		linkables.add(connectionStatus);

		controlPanel = new JPanel();
		contentPane.add(controlPanel, BorderLayout.CENTER);
		GridBagLayout gbl_controlPanel = new GridBagLayout();
		gbl_controlPanel.columnWeights = new double[] { 0.0, 0.0, 0.0 };
		gbl_controlPanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		controlPanel.setLayout(gbl_controlPanel);

		btnAhead = new SignalButton();
		btnAhead.setButtonText("Ahead");
		btnAhead.setId("ahead");
		btnAhead.setValue("100");
		btnAhead.setValueLabel("Strength");
		btnAhead.setIcon(AHEAD_ICON);
		linkables.add(btnAhead);
		GridBagConstraints gbc_btnUp = new GridBagConstraints();
		gbc_btnUp.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnUp.insets = new Insets(0, 0, 0, 5);
		gbc_btnUp.gridx = 1;
		gbc_btnUp.gridy = 0;
		controlPanel.add(btnAhead, gbc_btnUp);

		btnLeft = new SignalButton();
		btnLeft.setButtonText("Left");
		btnLeft.setId("left");
		btnLeft.setValue("100");
		btnLeft.setValueLabel("Strength");
		btnLeft.setIcon(LEFT_ICON);
		linkables.add(btnLeft);
		GridBagConstraints gbc_btnLeft = new GridBagConstraints();
		gbc_btnLeft.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnLeft.insets = new Insets(0, 0, 0, 5);
		gbc_btnLeft.gridx = 0;
		gbc_btnLeft.gridy = 1;
		controlPanel.add(btnLeft, gbc_btnLeft);

		btnRight = new SignalButton();
		btnRight.setButtonText("Right");
		btnRight.setId("right");
		btnRight.setValue("100");
		btnRight.setValueLabel("Strength");
		btnRight.setIcon(RIGHT_ICON);
		linkables.add(btnRight);
		GridBagConstraints gbc_btnRight = new GridBagConstraints();
		gbc_btnRight.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnRight.insets = new Insets(0, 0, 0, 5);
		gbc_btnRight.gridx = 2;
		gbc_btnRight.gridy = 1;
		controlPanel.add(btnRight, gbc_btnRight);

		btnBack = new SignalButton();
		btnBack.setButtonText("Back");
		btnBack.setId("back");
		btnBack.setValue("100");
		btnBack.setValueLabel("Strength");
		btnBack.setIcon(BACK_ICON);
		linkables.add(btnBack);
		GridBagConstraints gbc_btnDown = new GridBagConstraints();
		gbc_btnDown.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnDown.gridx = 1;
		gbc_btnDown.gridy = 2;
		controlPanel.add(btnBack, gbc_btnDown);

		setLink(bluetoothConnectionPanel.getLink());
	}

	@Override
	public void setLink(Link link) {
		this.link.removeConnectionListener(this.connectionListener);
		this.link = link;
		this.link.addConnectionListener(this.connectionListener);
		for (Linkable linkable : linkables) {
			linkable.setLink(this.link);
		}
	}

}
