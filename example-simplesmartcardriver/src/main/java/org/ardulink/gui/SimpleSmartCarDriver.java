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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
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
import org.ardulink.gui.customcomponents.SignalButton;
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
public class SimpleSmartCarDriver extends JFrame implements ConnectionListener,
		Linkable {

	private static final Logger logger = LoggerFactory.getLogger(SimpleSmartCarDriver.class);

	private static final long serialVersionUID = 6065022178316507177L;

	private JPanel contentPane;
	private Link link;
	private List<Linkable> linkables = Lists.newArrayList();

	private ConnectionPanel genericConnectionPanel;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private JPanel controlPanel;
	private SignalButton btnAhead;
	private SignalButton btnLeft;
	private SignalButton btnRight;
	private SignalButton btnBack;

	private static final String AHEAD_ICON_NAME = "icons/arrow-up.png";
	private static final String LEFT_ICON_NAME = "icons/arrow-left.png";
	private static final String RIGHT_ICON_NAME = "icons/arrow-right.png";
	private static final String BACK_ICON_NAME = "icons/arrow-down.png";

	private static final ImageIcon AHEAD_ICON = new ImageIcon(
			SimpleSmartCarDriver.class.getResource(AHEAD_ICON_NAME));
	private static final ImageIcon LEFT_ICON = new ImageIcon(
			SimpleSmartCarDriver.class.getResource(LEFT_ICON_NAME));
	private static final ImageIcon RIGHT_ICON = new ImageIcon(
			SimpleSmartCarDriver.class.getResource(RIGHT_ICON_NAME));
	private static final ImageIcon BACK_ICON = new ImageIcon(
			SimpleSmartCarDriver.class.getResource(BACK_ICON_NAME));
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
		
		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(tabbedPane, BorderLayout.NORTH);
		
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
																	setLink(genericConnectionPanel.createLink());
																} catch (Exception e) {
																	e.printStackTrace();
																	JOptionPane.showMessageDialog(SimpleSmartCarDriver.this, e.getMessage(),
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

	private void disconnect() {
		logger.info("Connection status: {}", !this.link.disconnect());
		setLink(Link.NO_LINK);
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
