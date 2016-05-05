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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
import org.ardulink.gui.customcomponents.joystick.ModifiableJoystick;
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
public class JoystickSmartCarDriver extends JFrame implements Linkable {

	private static final long serialVersionUID = 1402473246181814940L;

	private JPanel contentPane;
	private Link link;
	private List<Linkable> linkables = Lists.newArrayList();

	private BluetoothConnectionPanel bluetoothConnectionPanel;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private JPanel controlPanel;
	private ModifiableJoystick joystick;
	private MotorDriver motorDriver = new MotorDriver();

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

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					for (LookAndFeelInfo laf : UIManager
							.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(laf.getName())) {
							UIManager.setLookAndFeel(laf.getClassName());
						}
					}
					JoystickSmartCarDriver frame = new JoystickSmartCarDriver();
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
	public JoystickSmartCarDriver() {
		setTitle("Joystick Smart Car Driver");
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
			@Override
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
			@Override
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
		controlPanel.setLayout(new BorderLayout(0, 0));

		joystick = new ModifiableJoystick();
		// not use Joystick link, PositionEvents will be captured and managed
		// with a specific class
		joystick.setLink(null);
		joystick.setId("joy");
		joystick.addPositionListener(motorDriver);
		controlPanel.add(joystick, BorderLayout.CENTER);

		linkables.add(motorDriver);

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
