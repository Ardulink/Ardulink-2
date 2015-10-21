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

import static org.zu.ardulink.util.Strings.nullOrEmpty;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zu.ardulink.Link;
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DisconnectionEvent;
import org.zu.ardulink.gui.customcomponents.ModifiableSignalButton;
import org.zu.ardulink.gui.customcomponents.ModifiableToggleSignalButton;
import org.zu.ardulink.gui.customcomponents.joystick.ModifiableJoystick;
import org.zu.ardulink.gui.customcomponents.joystick.SimplePositionListener;
import org.zu.ardulink.gui.digistump.DigisparkConnectionPanel;
import org.zu.ardulink.protocol.ReplyMessageCallback;

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

	private static final long serialVersionUID = -5288916405936436557L;
	
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private KeyPressController keyControlPanel;
	private JRadioButton serialConnectionRadioButton;
	private JRadioButton networkConnectionRadioButton;
	private JRadioButton digisparkConnectionRadioButton;
	private JRadioButton bluetoothConnectionRadioButton;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private ConnectionStatus connectionStatus;
	
	private SerialConnectionPanel serialConnectionPanel;
	private NetworkProxyConnectionPanel networkProxyConnectionPanel;
	private DigisparkConnectionPanel digisparkConnectionPanel;
	private BluetoothConnectionPanel bluetoothConnectionPanel;
	
	private Link link;
	
	private final List<Linkable> linkables = new LinkedList<Linkable>();
	
	private static final Logger logger = LoggerFactory.getLogger(Console.class);
	
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
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				Console.class.getResource("icons/logo_icon.png")));
		setTitle("Ardulink Console");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 730, 620);
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
				
				if(serialConnectionRadioButton.isSelected() || networkConnectionRadioButton.isSelected()) {
					String comPort = null;
					String baudRateS = null;
					if(serialConnectionRadioButton.isSelected()) { // Serial Connection
						comPort = serialConnectionPanel.getConnectionPort();
						baudRateS = serialConnectionPanel.getBaudRate();
						
					} else if(networkConnectionRadioButton.isSelected()) { // Network Connection
						comPort = networkProxyConnectionPanel.getConnectionPort();
						baudRateS = serialConnectionPanel.getBaudRate();
					}
					if (nullOrEmpty(comPort)) {
						JOptionPane.showMessageDialog(btnConnect, "Invalid COM PORT setted.", "Error", JOptionPane.ERROR_MESSAGE);
					} else if (nullOrEmpty(baudRateS)) {
						JOptionPane.showMessageDialog(btnConnect, "Invalid baud rate setted. Advice: set " + Link.DEFAULT_BAUDRATE, "Error", JOptionPane.ERROR_MESSAGE);
					} else if (networkConnectionRadioButton.isSelected() && networkProxyConnectionPanel.getLink() == null) {
						JOptionPane.showMessageDialog(btnConnect, "Proxy is not activated", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						try {
							int baudRate = Integer.parseInt(baudRateS);
							
							if(networkConnectionRadioButton.isSelected()) {
								setLink(networkProxyConnectionPanel.getLink());
							}
							
							boolean connected = link.connect(comPort, baudRate);
							logConnectState(connected);
						}
						catch(Exception ex) {
							ex.printStackTrace();
							String message = ex.getMessage();
							if (nullOrEmpty(message)) {
								message = "Generic Error on connection";
							}
							JOptionPane.showMessageDialog(btnConnect, message, "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				} else if(bluetoothConnectionRadioButton.isSelected()) {
					try {
						String deviceName = bluetoothConnectionPanel.getSelectedDevice();
						setLink(bluetoothConnectionPanel.getLink());
						boolean connected = link.connect(deviceName);
						logConnectState(connected);
					}
					catch(Exception ex) {
						ex.printStackTrace();
						String message = ex.getMessage();
						if (nullOrEmpty(message)) {
							message = "Generic Error on connection";
						}
						JOptionPane.showMessageDialog(btnConnect, message, "Error", JOptionPane.ERROR_MESSAGE);
					}

				} else { // Digispark Connection
					try {
						String deviceName = digisparkConnectionPanel.getSelectedDevice();
						setLink(digisparkConnectionPanel.getLink());
						boolean connected = link.connect(deviceName);
						logConnectState(connected);
					}
					catch(Exception ex) {
						ex.printStackTrace();
						String message = ex.getMessage();
						if (nullOrEmpty(message)) {
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
					logConnectState(connected);
					
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
				digisparkConnectionPanel.setEnabled(false);
				bluetoothConnectionPanel.setEnabled(false);
				setLink(serialConnectionPanel.getLink());
			}
		});
		serialConnectionRadioButton.setSelected(true);
		GridBagConstraints gbc_serialConnectionRadioButton = new GridBagConstraints();
		gbc_serialConnectionRadioButton.insets = new Insets(0, 0, 0, 10);
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
				digisparkConnectionPanel.setEnabled(false);
				bluetoothConnectionPanel.setEnabled(false);
				setLink(networkProxyConnectionPanel.getLink());
			}
		});
		GridBagConstraints gbc_networkConnectionRadioButton = new GridBagConstraints();
		gbc_networkConnectionRadioButton.insets = new Insets(0, 10, 0, 0);
		gbc_networkConnectionRadioButton.anchor = GridBagConstraints.NORTH;
		gbc_networkConnectionRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_networkConnectionRadioButton.gridx = 1;
		gbc_networkConnectionRadioButton.gridy = 0;
		allConnectionsPanel.add(networkConnectionRadioButton, gbc_networkConnectionRadioButton);
		
		digisparkConnectionRadioButton = new JRadioButton("Digispark Connection");
		digisparkConnectionRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serialConnectionPanel.setEnabled(false);
				networkProxyConnectionPanel.setEnabled(false);
				digisparkConnectionPanel.setEnabled(true);
				bluetoothConnectionPanel.setEnabled(false);
				setLink(digisparkConnectionPanel.getLink());
			}
		});
		digisparkConnectionRadioButton.setSelected(false);
		GridBagConstraints gbc_digisparkConnectionRadioButton = new GridBagConstraints();
		gbc_digisparkConnectionRadioButton.insets = new Insets(20, 0, 0, 10);
		gbc_digisparkConnectionRadioButton.anchor = GridBagConstraints.NORTH;
		gbc_digisparkConnectionRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_digisparkConnectionRadioButton.gridx = 0;
		gbc_digisparkConnectionRadioButton.gridy = 2;
		allConnectionsPanel.add(digisparkConnectionRadioButton, gbc_digisparkConnectionRadioButton);

		bluetoothConnectionRadioButton = new JRadioButton("Bluetooth Connection");
		bluetoothConnectionRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serialConnectionPanel.setEnabled(false);
				networkProxyConnectionPanel.setEnabled(false);
				digisparkConnectionPanel.setEnabled(false);
				bluetoothConnectionPanel.setEnabled(true);
				setLink(bluetoothConnectionPanel.getLink());
			}
		});
		bluetoothConnectionRadioButton.setSelected(false);
		GridBagConstraints gbc_bluetoothConnectionRadioButton = new GridBagConstraints();
		gbc_bluetoothConnectionRadioButton.insets = new Insets(20, 0, 0, 10);
		gbc_bluetoothConnectionRadioButton.anchor = GridBagConstraints.NORTH;
		gbc_bluetoothConnectionRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_bluetoothConnectionRadioButton.gridx = 0;
		gbc_bluetoothConnectionRadioButton.gridy = 4;
		allConnectionsPanel.add(bluetoothConnectionRadioButton, gbc_bluetoothConnectionRadioButton);

		serialConnectionPanel = new SerialConnectionPanel();
		serialConnectionPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		GridBagConstraints gbc_serialConnectionPanel = new GridBagConstraints();
		gbc_serialConnectionPanel.insets = new Insets(0, 0, 0, 10);
		gbc_serialConnectionPanel.anchor = GridBagConstraints.NORTH;
		gbc_serialConnectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_serialConnectionPanel.gridx = 0;
		gbc_serialConnectionPanel.gridy = 1;
		allConnectionsPanel.add(serialConnectionPanel, gbc_serialConnectionPanel);
		
		networkProxyConnectionPanel = new NetworkProxyConnectionPanel();
		networkProxyConnectionPanel.setEnabled(false);
		networkProxyConnectionPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		GridBagConstraints gbc_networkProxyConnectionPanel = new GridBagConstraints();
		gbc_networkProxyConnectionPanel.gridheight = 3;
		gbc_networkProxyConnectionPanel.insets = new Insets(0, 10, 0, 0);
		gbc_networkProxyConnectionPanel.anchor = GridBagConstraints.SOUTH;
		gbc_networkProxyConnectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_networkProxyConnectionPanel.gridx = 1;
		gbc_networkProxyConnectionPanel.gridy = 1;
		allConnectionsPanel.add(networkProxyConnectionPanel, gbc_networkProxyConnectionPanel);
		
		digisparkConnectionPanel = new DigisparkConnectionPanel();
		digisparkConnectionPanel.setEnabled(false);
		digisparkConnectionPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		GridBagConstraints gbc_digisparkConnectionPanel = new GridBagConstraints();
		gbc_digisparkConnectionPanel.gridheight = 1;
		gbc_digisparkConnectionPanel.insets = new Insets(0, 0, 0, 10);
		gbc_digisparkConnectionPanel.anchor = GridBagConstraints.NORTH;
		gbc_digisparkConnectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_digisparkConnectionPanel.gridx = 0;
		gbc_digisparkConnectionPanel.gridy = 3;
		allConnectionsPanel.add(digisparkConnectionPanel, gbc_digisparkConnectionPanel);

		bluetoothConnectionPanel = new BluetoothConnectionPanel();
		bluetoothConnectionPanel.setEnabled(false);
		bluetoothConnectionPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		GridBagConstraints gbc_bluetoothConnectionPanel = new GridBagConstraints();
		gbc_bluetoothConnectionPanel.gridheight = 1;
		gbc_bluetoothConnectionPanel.insets = new Insets(0, 0, 0, 10);
		gbc_bluetoothConnectionPanel.anchor = GridBagConstraints.NORTH;
		gbc_bluetoothConnectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_bluetoothConnectionPanel.gridx = 0;
		gbc_bluetoothConnectionPanel.gridy = 5;
		allConnectionsPanel.add(bluetoothConnectionPanel, gbc_bluetoothConnectionPanel);

		//Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
	    group.add(serialConnectionRadioButton);
	    group.add(networkConnectionRadioButton);
	    group.add(digisparkConnectionRadioButton);
	    group.add(bluetoothConnectionRadioButton);
		
		keyControlPanel = new KeyPressController();
		linkables.add(keyControlPanel);
		tabbedPane.addTab("Key Control", null, keyControlPanel, null);
				
		JPanel powerPanel = new JPanel();
		tabbedPane.addTab("Power Panel", null, powerPanel, null);
		powerPanel.setLayout(new GridLayout(2, 3, 0, 0));
		
		for (int pin = 3; pin <= 11; pin++) {
			powerPanel.add(pwmController(pin));
		}
		
		JPanel switchPanel = new JPanel();
		tabbedPane.addTab("Switch Panel", null, switchPanel, null);
		switchPanel.setLayout(new GridLayout(5, 3, 0, 0));
				
		for (int pin = 3; pin <= 13; pin++) {
			switchPanel.add(switchController(pin));
		}
		
		JPanel joystickPanel = new JPanel();
		tabbedPane.addTab("Joystick Panel", null, joystickPanel, null);
		joystickPanel.setLayout(new GridLayout(2, 2, 0, 0));
		
		ModifiableJoystick joy1 = modifiableJoystick("Left");
		joystickPanel.add(joy1);
		joystickPanel.add(simplePositionListener(joy1));

		ModifiableJoystick joy2 = modifiableJoystick("Right");
		joystickPanel.add(joy2);
		joystickPanel.add(simplePositionListener(joy2));
		
		JPanel sensorDigitalPanel = new JPanel();
		tabbedPane.addTab("Digital Sensor Panel", null, sensorDigitalPanel, null);
		sensorDigitalPanel.setLayout(new GridLayout(4, 3, 0, 0));
		
		for (int pin = 2; pin <= 12; pin++) {
			sensorDigitalPanel.add(digitalPinStatus(pin));
		}
		
		JPanel sensorAnalogPanel = new JPanel();
		sensorAnalogPanel.setLayout(new GridLayout(2, 3, 0, 0));
		tabbedPane.addTab("Analog Sensor Panel", null, sensorAnalogPanel, null);

		for (int pin = 0; pin <= 5; pin++) {
			sensorAnalogPanel.add(analogPinStatus(pin));
		}
		
		JPanel customPanel = new JPanel();
		tabbedPane.addTab("Custom Components", null, customPanel, null);
		customPanel.setLayout(new GridLayout(2, 3, 10, 15));

		for (int i = 0; i <= 2; i++) {
			customPanel.add(modifiableSignalButton());
		}

		for (int i = 0; i <= 2; i++) {
			customPanel.add(modifiableToggleSignalButton());
		}
		
		JPanel tonePanel = new JPanel();
		tabbedPane.addTab("Tone Panel", null, tonePanel, null);
		
		ToneController toneController = new ToneController();
		tonePanel.add(toneController);
		linkables.add(toneController);
		
		JPanel rgbPanel = new JPanel();
		tabbedPane.addTab("RGB Panel", null, rgbPanel, null);
		
		RGBController rgbController = new RGBController();
		linkables.add(rgbController);
		rgbPanel.add(rgbController);
		
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


	private PWMController pwmController(int pin) {
		PWMController pwmController = new PWMController();
		pwmController.setPin(pin);
		linkables.add(pwmController);
		return pwmController;
	}


	private SimplePositionListener simplePositionListener(
			ModifiableJoystick joystick) {
		SimplePositionListener positionListener = new SimplePositionListener();
		joystick.addPositionListener(positionListener);
		return positionListener;
	}

	private ModifiableJoystick modifiableJoystick(String id) {
		ModifiableJoystick joy1 = new ModifiableJoystick();
		joy1.setId(id);
		linkables.add(joy1);
		return joy1;
	}




	private ModifiableToggleSignalButton modifiableToggleSignalButton() {
		ModifiableToggleSignalButton modifiableToggleSignalButton1 = new ModifiableToggleSignalButton();
		linkables.add(modifiableToggleSignalButton1);
		return modifiableToggleSignalButton1;
	}


	private SwitchController switchController(int pin) {
		SwitchController switchController = new SwitchController();
		switchController.setPin(pin);
		linkables.add(switchController);
		return switchController;
	}

	private AnalogPinStatus analogPinStatus(int pin) {
		AnalogPinStatus analogPinStatus = new AnalogPinStatus();
		analogPinStatus.setPin(pin);
		linkables.add(analogPinStatus);
		return analogPinStatus;
	}

	private ModifiableSignalButton modifiableSignalButton() {
		ModifiableSignalButton modifiableSignalButton1 = new ModifiableSignalButton();
		linkables.add(modifiableSignalButton1);
		return modifiableSignalButton1;
	}
	
	private DigitalPinStatus digitalPinStatus(int pin) {
		DigitalPinStatus digitalPinStatus2 = new DigitalPinStatus();
		digitalPinStatus2.setPin(pin);
		linkables.add(digitalPinStatus2);
		return digitalPinStatus2;
	}
	
	public void setLink(Link link) {
		if (this.link != null) {
			this.link.removeConnectionListener(this);
		}
		this.link = link;
		if (link == null) {
			disconnected(new DisconnectionEvent());
		} else {
			link.addConnectionListener(this);
		}
		for (Linkable linkable : linkables) {
			linkable.setLink(link);
		}
	}

	@Override
	public void connected(ConnectionEvent e) {
		serialConnectionRadioButton.setEnabled(false);
		networkConnectionRadioButton.setEnabled(false);
		digisparkConnectionRadioButton.setEnabled(false);
		
		if(serialConnectionRadioButton.isSelected()) {
			serialConnectionPanel.setEnabled(false);
		} else if(networkConnectionRadioButton.isSelected()) {
			networkProxyConnectionPanel.setEnabled(false);
		} else if(digisparkConnectionRadioButton.isSelected()) {
			digisparkConnectionPanel.setEnabled(false);
		}
		
		btnConnect.setEnabled(false);
		btnDisconnect.setEnabled(true);
	}

	@Override
	public void disconnected(DisconnectionEvent e) {
		serialConnectionRadioButton.setEnabled(true);
		networkConnectionRadioButton.setEnabled(true);
		digisparkConnectionRadioButton.setEnabled(true);

		if(serialConnectionRadioButton.isSelected()) {
			serialConnectionPanel.setEnabled(true);
		} else if(networkConnectionRadioButton.isSelected()) {
			networkProxyConnectionPanel.setEnabled(true);
		} else if(digisparkConnectionRadioButton.isSelected()) {
			digisparkConnectionPanel.setEnabled(true);
		}
		
		btnConnect.setEnabled(true);
		btnDisconnect.setEnabled(false);
	}

	private void logConnectState(boolean connected) {
		logger.info("Connection status: ", connected);
	}

	public ReplyMessageCallback getReplyMessageCallback() {
		throw new RuntimeException("Not developed yet");
	}

	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		throw new RuntimeException("Not developed yet");
	}	
}
