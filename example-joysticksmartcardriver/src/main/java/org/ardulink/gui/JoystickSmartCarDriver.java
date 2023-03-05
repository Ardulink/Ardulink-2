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

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static org.ardulink.gui.facility.LAFUtil.setLookAndFeel;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.ConnectionListener;
import org.ardulink.gui.connectionpanel.ConnectionPanel;
import org.ardulink.gui.customcomponents.joystick.ModifiableJoystick;
import org.ardulink.legacy.Link;
import org.ardulink.legacy.Link.LegacyLinkAdapter;
import org.ardulink.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class JoystickSmartCarDriver extends JFrame implements
		ConnectionListener, Linkable {

	private static final long serialVersionUID = 1402473246181814940L;

	private static final Logger logger = LoggerFactory.getLogger(JoystickSmartCarDriver.class);
	
	private JPanel contentPane;
	private Link link;
	private List<Linkable> linkables = Lists.newArrayList();

	private ConnectionPanel genericConnectionPanel;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private JPanel controlPanel;
	private ModifiableJoystick joystick;
	private MotorDriver motorDriver = new MotorDriver();
	private JTabbedPane tabbedPane;
	private JPanel buttonPanel;

	/**
	 * Launch the application. 
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					setLookAndFeel("Nimbus");
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
		
		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
				JPanel connectionPanel = new JPanel();
				tabbedPane.addTab("Connection", null, connectionPanel, null);
						connectionPanel.setLayout(new BorderLayout(0, 0));
				
						genericConnectionPanel = new ConnectionPanel();
						connectionPanel.add(genericConnectionPanel, BorderLayout.CENTER);
												
												buttonPanel = new JPanel();
												connectionPanel.add(buttonPanel, BorderLayout.SOUTH);
												
														btnConnect = new JButton("Connect");
														buttonPanel.add(btnConnect);
														
																btnDisconnect = new JButton("Disconnect");
																buttonPanel.add(btnDisconnect);
																btnDisconnect.addActionListener(new ActionListener() {
																	@Override
																	public void actionPerformed(ActionEvent e) {
																		disconnect();
																	}

																});
																btnDisconnect.setEnabled(false);
																
																		ConnectionStatus connectionStatus = new ConnectionStatus();
																		buttonPanel.add(connectionStatus);
																		linkables.add(connectionStatus);
														btnConnect.addActionListener(new ActionListener() {
															@Override
															public void actionPerformed(ActionEvent event) {
																try {
																	setLink((genericConnectionPanel.createLink()));
																} catch (Exception e) {
																	e.printStackTrace();
																	JOptionPane.showMessageDialog(JoystickSmartCarDriver.this, e.getMessage(),
																			"Error", ERROR_MESSAGE);
																}
															}

															private LegacyLinkAdapter legacyAdapt(
																	org.ardulink.core.Link link) {
																return new Link.LegacyLinkAdapter(link);
															}

															
														});
												
														controlPanel = new JPanel();
														tabbedPane.addTab("Control", null, controlPanel, null);
														controlPanel.setLayout(new BorderLayout(0, 0));
														
																joystick = new ModifiableJoystick();
																// not use Joystick link, PositionEvents will be captured and managed
																// with a specific class
																joystick.setLink(null);
																joystick.setId("joy");
																joystick.addPositionListener(motorDriver);
																controlPanel.add(joystick, BorderLayout.CENTER);

		linkables.add(motorDriver);

		setLink(Link.NO_LINK);
	}

	private void disconnect() {
		logger.info("Connection status: {}", !this.link.disconnect());
		setLink(Link.NO_LINK);
	}
	
	@Override
	public void setLink(Link link) {
		org.ardulink.core.Link delegate = link.getDelegate();
		if (delegate instanceof ConnectionBasedLink) {
			((ConnectionBasedLink) delegate).removeConnectionListener(this);
		}
		this.link = link;
		if (delegate instanceof ConnectionBasedLink) {
			((ConnectionBasedLink) delegate).addConnectionListener(this);
		} else {
			if(link == null || link == Link.NO_LINK) {
				connectionLost();
			} else {
				reconnected();
			}
			
		}
		for (Linkable linkable : linkables) {
			linkable.setLink(link);
		}
	}

	@Override
	public void reconnected() {
		genericConnectionPanel.setEnabled(false);
		btnConnect.setEnabled(false);
		btnDisconnect.setEnabled(true);
	}

	@Override
	public void connectionLost() {
		genericConnectionPanel.setEnabled(true);
		btnConnect.setEnabled(true);
		btnDisconnect.setEnabled(false);
	}
}

