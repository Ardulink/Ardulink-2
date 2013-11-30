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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.zu.ardulink.Link;
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DisconnectionEvent;

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 * [ardulinktitle] [ardulinkversion]
 * This is the ready ardulink console a complete SWING application to manage an Arduino board.
 * Console has several tabs with all ready arduino components. Each tab is able to
 * do a specific action sending or listening for messages to arduino or from arduino board.
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Console extends JFrame implements ConnectionListener, Linkable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5288916405936436557L;
	
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private KeyPressController keyControlPanel;
	private JRadioButton serialConnectionRadioButton;
	private JRadioButton networkConnectionRadioButton;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private ConnectionStatus connectionStatus;
	
	private ConnectionPanel serialConnectionPanel;
	private NetworkProxyConnectionPanel networkProxyConnectionPanel;
	
	private Link link = null;
	
	private List<Linkable> linkables = new LinkedList<Linkable>();
	
	private static Logger logger = Logger.getLogger(Console.class.getName());
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					UIManager.setLookAndFeel(NimbusLookAndFeel.class.getCanonicalName());
					Console frame = new Console();					
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
	public Console() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(Console.class.getResource("/org/zu/ardulink/gui/icons/logo_icon.png")));
		setTitle("Ardulink Console");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 590);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel configurationPanel = new JPanel();
		tabbedPane.addTab("Configuration", null, configurationPanel, null);
		configurationPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel connectPanel = new JPanel();
		configurationPanel.add(connectPanel, BorderLayout.SOUTH);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String comPort = null;
				String baudRateS = null;
				if(serialConnectionRadioButton.isSelected()) { // Serial Connection
					comPort = serialConnectionPanel.getConnectionPort();
					baudRateS = serialConnectionPanel.getBaudRate();
					
				} else { // Network Connection
					comPort = networkProxyConnectionPanel.getConnectionPort();
					baudRateS = serialConnectionPanel.getBaudRate();
				}
				if(comPort == null || "".equals(comPort)) {
					JOptionPane.showMessageDialog(btnConnect, "Invalid COM PORT setted.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if(baudRateS == null || "".equals(baudRateS)) {
					JOptionPane.showMessageDialog(btnConnect, "Invalid baud rate setted. Advice: set " + Link.DEFAULT_BAUDRATE, "Error", JOptionPane.ERROR_MESSAGE);
				} else if(networkConnectionRadioButton.isSelected() && networkProxyConnectionPanel.getLink() == null) {
					JOptionPane.showMessageDialog(btnConnect, "Proxy is not activated", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						int baudRate = Integer.parseInt(baudRateS);
						
						if(networkConnectionRadioButton.isSelected()) {
							setLink(networkProxyConnectionPanel.getLink());
						}
						
						boolean connected = link.connect(comPort, baudRate);
						logger.info("Connection status: " + connected);
					}
					catch(Exception ex) {
						ex.printStackTrace();
						String message = ex.getMessage();
						if(message == null || message.trim().equals("")) {
							message = "Generic Error on connection";
						}
						JOptionPane.showMessageDialog(btnConnect, message, "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		connectPanel.add(btnConnect);
		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(link != null) {
					boolean connected = !link.disconnect();
					logger.info("Connection status: " + connected);
					
					if(networkConnectionRadioButton.isSelected()) {
						networkProxyConnectionPanel.destroyLink();
					}
				}
			}
		});
		connectPanel.add(btnDisconnect);
		
		JPanel allConnectionsPanel = new JPanel();
		configurationPanel.add(allConnectionsPanel, BorderLayout.CENTER);
		GridBagLayout gbl_allConnectionsPanel = new GridBagLayout();
		gbl_allConnectionsPanel.columnWeights = new double[]{0.0, 0.0};
		gbl_allConnectionsPanel.rowWeights = new double[]{0.0, 0.0};
		allConnectionsPanel.setLayout(gbl_allConnectionsPanel);
		
		serialConnectionRadioButton = new JRadioButton("Serial Connection");
		serialConnectionRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serialConnectionPanel.setEnabled(true);
				networkProxyConnectionPanel.setEnabled(false);
				setLink(serialConnectionPanel.getLink());
			}
		});
		serialConnectionRadioButton.setSelected(true);
		GridBagConstraints gbc_serialConnectionRadioButton = new GridBagConstraints();
		gbc_serialConnectionRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_serialConnectionRadioButton.anchor = GridBagConstraints.NORTH;
		gbc_serialConnectionRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_serialConnectionRadioButton.gridx = 0;
		gbc_serialConnectionRadioButton.gridy = 0;
		allConnectionsPanel.add(serialConnectionRadioButton, gbc_serialConnectionRadioButton);
		
		networkConnectionRadioButton = new JRadioButton("Network Connection");
		networkConnectionRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serialConnectionPanel.setEnabled(false);
				networkProxyConnectionPanel.setEnabled(true);
				setLink(networkProxyConnectionPanel.getLink());
			}
		});
		GridBagConstraints gbc_networkConnectionRadioButton = new GridBagConstraints();
		gbc_networkConnectionRadioButton.insets = new Insets(0, 5, 0, 0);
		gbc_networkConnectionRadioButton.anchor = GridBagConstraints.NORTH;
		gbc_networkConnectionRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_networkConnectionRadioButton.gridx = 1;
		gbc_networkConnectionRadioButton.gridy = 0;
		allConnectionsPanel.add(networkConnectionRadioButton, gbc_networkConnectionRadioButton);
		
		serialConnectionPanel = new ConnectionPanel();
		serialConnectionPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		serialConnectionPanel.setLayout(null);
		GridBagConstraints gbc_serialConnectionPanel = new GridBagConstraints();
		gbc_serialConnectionPanel.insets = new Insets(0, 0, 0, 5);
		gbc_serialConnectionPanel.anchor = GridBagConstraints.SOUTH;
		gbc_serialConnectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_serialConnectionPanel.gridx = 0;
		gbc_serialConnectionPanel.gridy = 1;
		allConnectionsPanel.add(serialConnectionPanel, gbc_serialConnectionPanel);
		
		networkProxyConnectionPanel = new NetworkProxyConnectionPanel();
		networkProxyConnectionPanel.setEnabled(false);
		networkProxyConnectionPanel.setPreferredSize(new Dimension(240, 390));
		networkProxyConnectionPanel.setMinimumSize(new Dimension(240, 390));
		networkProxyConnectionPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		networkProxyConnectionPanel.setLayout(null);
		GridBagConstraints gbc_networkProxyConnectionPanel = new GridBagConstraints();
		gbc_networkProxyConnectionPanel.insets = new Insets(0, 5, 0, 0);
		gbc_networkProxyConnectionPanel.anchor = GridBagConstraints.SOUTH;
		gbc_networkProxyConnectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_networkProxyConnectionPanel.gridx = 1;
		gbc_networkProxyConnectionPanel.gridy = 1;
		allConnectionsPanel.add(networkProxyConnectionPanel, gbc_networkProxyConnectionPanel);
		
		//Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
	    group.add(serialConnectionRadioButton);
	    group.add(networkConnectionRadioButton);
		
		keyControlPanel = new KeyPressController();
		linkables.add(keyControlPanel);
		tabbedPane.addTab("Key Control", null, keyControlPanel, null);
				
		JPanel powerPanel = new JPanel();
		tabbedPane.addTab("Power Panel", null, powerPanel, null);
		powerPanel.setLayout(new GridLayout(2, 3, 0, 0));
		
		PWMController panelPin3 = new PWMController();
		linkables.add(panelPin3);
		panelPin3.setPin(3);
		powerPanel.add(panelPin3);
		
		PWMController panelPin5 = new PWMController();
		linkables.add(panelPin5);
		panelPin5.setPin(5);
		powerPanel.add(panelPin5);
		
		PWMController panelPin6 = new PWMController();
		linkables.add(panelPin6);
		panelPin6.setPin(6);
		powerPanel.add(panelPin6);
		
		PWMController panelPin9 = new PWMController();
		linkables.add(panelPin9);
		panelPin9.setPin(9);
		powerPanel.add(panelPin9);
		
		PWMController panelPin10 = new PWMController();
		linkables.add(panelPin10);
		panelPin10.setPin(10);
		powerPanel.add(panelPin10);
		
		PWMController panelPin11 = new PWMController();
		linkables.add(panelPin11);
		panelPin11.setPin(11);
		powerPanel.add(panelPin11);
				
		JPanel switchPanel = new JPanel();
		tabbedPane.addTab("Switch Panel", null, switchPanel, null);
		switchPanel.setLayout(new GridLayout(5, 3, 0, 0));
				
		SwitchController switchController3 = new SwitchController();
		linkables.add(switchController3);
		switchController3.setPin(3);
		switchPanel.add(switchController3);
		
		SwitchController switchController4 = new SwitchController();
		linkables.add(switchController4);
		switchController4.setPin(4);
		switchPanel.add(switchController4);
		
		SwitchController switchController5 = new SwitchController();
		linkables.add(switchController5);
		switchController5.setPin(5);
		switchPanel.add(switchController5);
		
		SwitchController switchController6 = new SwitchController();
		linkables.add(switchController6);
		switchController6.setPin(6);
		switchPanel.add(switchController6);
		
		SwitchController switchController7 = new SwitchController();
		linkables.add(switchController7);
		switchController7.setPin(7);
		switchPanel.add(switchController7);
		
		SwitchController switchController8 = new SwitchController();
		linkables.add(switchController8);
		switchController8.setPin(8);
		switchPanel.add(switchController8);
		
		SwitchController switchController9 = new SwitchController();
		linkables.add(switchController9);
		switchController9.setPin(9);
		switchPanel.add(switchController9);
		
		SwitchController switchController10 = new SwitchController();
		linkables.add(switchController10);
		switchController10.setPin(10);
		switchPanel.add(switchController10);
		
		SwitchController switchController11 = new SwitchController();
		linkables.add(switchController11);
		switchController11.setPin(11);
		switchPanel.add(switchController11);
		
		SwitchController switchController12 = new SwitchController();
		linkables.add(switchController12);
		switchController12.setPin(12);
		switchPanel.add(switchController12);
		
		SwitchController switchController13 = new SwitchController();
		linkables.add(switchController13);
		switchController13.setPin(13);
		switchPanel.add(switchController13);
		
		JPanel sensorDigitalPanel = new JPanel();
		tabbedPane.addTab("Sensor Digital Panel", null, sensorDigitalPanel, null);
		sensorDigitalPanel.setLayout(new GridLayout(4, 3, 0, 0));
		
		DigitalPinStatus digitalPinStatus2 = new DigitalPinStatus();
		linkables.add(digitalPinStatus2);
		digitalPinStatus2.setPin(2);
		sensorDigitalPanel.add(digitalPinStatus2);
		
		DigitalPinStatus digitalPinStatus3 = new DigitalPinStatus();
		linkables.add(digitalPinStatus3);
		digitalPinStatus3.setPin(3);
		sensorDigitalPanel.add(digitalPinStatus3);
		
		DigitalPinStatus digitalPinStatus4 = new DigitalPinStatus();
		linkables.add(digitalPinStatus4);
		digitalPinStatus4.setPin(4);
		sensorDigitalPanel.add(digitalPinStatus4);
		
		DigitalPinStatus digitalPinStatus5 = new DigitalPinStatus();
		linkables.add(digitalPinStatus5);
		digitalPinStatus5.setPin(5);
		sensorDigitalPanel.add(digitalPinStatus5);
		
		DigitalPinStatus digitalPinStatus6 = new DigitalPinStatus();
		linkables.add(digitalPinStatus6);
		digitalPinStatus6.setPin(6);
		sensorDigitalPanel.add(digitalPinStatus6);
		
		DigitalPinStatus digitalPinStatus7 = new DigitalPinStatus();
		linkables.add(digitalPinStatus7);
		digitalPinStatus7.setPin(7);
		sensorDigitalPanel.add(digitalPinStatus7);
		
		DigitalPinStatus digitalPinStatus8 = new DigitalPinStatus();
		linkables.add(digitalPinStatus8);
		digitalPinStatus8.setPin(8);
		sensorDigitalPanel.add(digitalPinStatus8);
		
		DigitalPinStatus digitalPinStatus9 = new DigitalPinStatus();
		linkables.add(digitalPinStatus9);
		digitalPinStatus9.setPin(9);
		sensorDigitalPanel.add(digitalPinStatus9);
		
		DigitalPinStatus digitalPinStatus10 = new DigitalPinStatus();
		linkables.add(digitalPinStatus10);
		digitalPinStatus10.setPin(10);
		sensorDigitalPanel.add(digitalPinStatus10);
		
		DigitalPinStatus digitalPinStatus11 = new DigitalPinStatus();
		linkables.add(digitalPinStatus11);
		digitalPinStatus11.setPin(11);
		sensorDigitalPanel.add(digitalPinStatus11);
		
		DigitalPinStatus digitalPinStatus12 = new DigitalPinStatus();
		linkables.add(digitalPinStatus12);
		digitalPinStatus12.setPin(12);
		sensorDigitalPanel.add(digitalPinStatus12);
		
		DigitalPinStatus digitalPinStatus13 = new DigitalPinStatus();
		linkables.add(digitalPinStatus13);
		digitalPinStatus13.setPin(13);
		sensorDigitalPanel.add(digitalPinStatus13);
		
		JPanel stateBar = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) stateBar.getLayout();
		flowLayout_1.setVgap(0);
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		contentPane.add(stateBar, BorderLayout.SOUTH);

		connectionStatus = new ConnectionStatus();
		linkables.add(connectionStatus);
		FlowLayout flowLayout = (FlowLayout) connectionStatus.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setAlignment(FlowLayout.LEFT);
		stateBar.add(connectionStatus, BorderLayout.SOUTH);

		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(tabbedPane.getSelectedComponent().equals(keyControlPanel)) {
					keyControlPanel.requestFocus();
				}
			}
		});

		setLink(serialConnectionPanel.getLink());
	}

	public void setLink(Link link) {
		if(this.link != null) {
			this.link.removeConnectionListener(this);
		} 
		this.link = link;
		if(link != null) {
			link.addConnectionListener(this);
		} else {
			disconnected(new DisconnectionEvent());
		}
		Iterator<Linkable> it = linkables.iterator();
		while(it.hasNext()) {
			it.next().setLink(link);
		}
	}

	@Override
	public void connected(ConnectionEvent e) {
		serialConnectionRadioButton.setEnabled(false);
		networkConnectionRadioButton.setEnabled(false);
		btnConnect.setEnabled(false);
		btnDisconnect.setEnabled(true);
	}

	@Override
	public void disconnected(DisconnectionEvent e) {
		serialConnectionRadioButton.setEnabled(true);
		networkConnectionRadioButton.setEnabled(true);
		btnConnect.setEnabled(true);
		btnDisconnect.setEnabled(false);
	}
}
