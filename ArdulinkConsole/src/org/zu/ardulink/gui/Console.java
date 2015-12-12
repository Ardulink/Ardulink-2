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
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zu.ardulink.gui.customcomponents.ModifiableSignalButton;
import org.zu.ardulink.gui.customcomponents.ModifiableToggleSignalButton;
import org.zu.ardulink.gui.customcomponents.joystick.ModifiableJoystick;
import org.zu.ardulink.gui.customcomponents.joystick.SimplePositionListener;
import org.zu.ardulink.legacy.Link;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;

/**
 * [ardulinktitle] [ardulinkversion] This is the ready ardulink console a
 * complete SWING application to manage an Arduino board. Console has several
 * tabs with all ready arduino components. Each tab is able to do a specific
 * action sending or listening for messages to arduino or from arduino board.
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Console extends JFrame implements Linkable {

	private static final long serialVersionUID = -5288916405936436557L;

	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private KeyPressController keyControlPanel;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private ConnectionStatus connectionStatus;

	private Link link;

	private final List<Linkable> linkables = new LinkedList<Linkable>();

	private static final Logger logger = LoggerFactory.getLogger(Console.class);

	private GenericConnectionPanel genericConnectionPanel;

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
			public void actionPerformed(ActionEvent event) {
				String uri = genericConnectionPanel.getURI();
				try {
					setLink(new Link.LegacyLinkAdapter(LinkManager
							.getInstance().getConfigurer(new URI(uri))
							.newLink()));
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		connectPanel.add(btnConnect);

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (link != null) {
					logger.info("Connection status: {}", !link.disconnect());
					setLink(null);
				}
			}

		});
		connectPanel.add(btnDisconnect);

		JPanel allConnectionsPanel = new JPanel();
		configurationPanel.add(allConnectionsPanel, BorderLayout.CENTER);
		GridBagLayout gbl_allConnectionsPanel = new GridBagLayout();
		gbl_allConnectionsPanel.columnWeights = new double[] { 0.0, 0.0 };
		gbl_allConnectionsPanel.rowWeights = new double[] { 0.0, 0.0 };
		allConnectionsPanel.setLayout(gbl_allConnectionsPanel);

		genericConnectionPanel = new GenericConnectionPanel();
		genericConnectionPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		GridBagConstraints gbc_genericConnectionPanel = new GridBagConstraints();
		gbc_genericConnectionPanel.insets = new Insets(0, 0, 0, 10);
		gbc_genericConnectionPanel.anchor = GridBagConstraints.NORTH;
		gbc_genericConnectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_genericConnectionPanel.gridx = 0;
		gbc_genericConnectionPanel.gridy = 1;
		allConnectionsPanel.add(genericConnectionPanel,
				gbc_genericConnectionPanel);

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
		tabbedPane.addTab("Digital Sensor Panel", null, sensorDigitalPanel,
				null);
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
				if (tabbedPane.getSelectedComponent().equals(keyControlPanel)) {
					keyControlPanel.requestFocus();
				}
			}
		});
		disconnected();
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
		this.link = link;
		if (link == null) {
			disconnected();
		} else {
			connected();
			connectionStatus.reconnected();
		}
		for (Linkable linkable : linkables) {
			linkable.setLink(link);
		}
	}

	private void connected() {
		btnConnect.setEnabled(false);
		btnDisconnect.setEnabled(true);
		genericConnectionPanel.setEnabled(false);
	}

	private void disconnected() {
		btnConnect.setEnabled(true);
		btnDisconnect.setEnabled(false);
		genericConnectionPanel.setEnabled(true);
	}

}
