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
package org.ardulink.console;

import static java.awt.EventQueue.invokeLater;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static org.ardulink.gui.Icons.icon;
import static org.ardulink.gui.facility.LAFUtil.setLookAndFeel;
import static org.ardulink.util.Throwables.getCauses;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ardulink.core.ConnectionListener;
import org.ardulink.gui.AnalogPinStatus;
import org.ardulink.gui.ConnectionStatus;
import org.ardulink.gui.DigitalPinStatus;
import org.ardulink.gui.KeyPressController;
import org.ardulink.gui.Linkable;
import org.ardulink.gui.PWMController;
import org.ardulink.gui.RGBController;
import org.ardulink.gui.SwitchController;
import org.ardulink.gui.ToneController;
import org.ardulink.gui.connectionpanel.ConnectionPanel;
import org.ardulink.gui.customcomponents.ModifiableSignalButton;
import org.ardulink.gui.customcomponents.ModifiableToggleSignalButton;
import org.ardulink.gui.customcomponents.joystick.ModifiableJoystick;
import org.ardulink.gui.customcomponents.joystick.SimplePositionListener;
import org.ardulink.gui.serial.SerialMonitor;
import org.ardulink.legacy.Link;
import org.ardulink.util.Streams;
import org.ardulink.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [ardulinktitle] [ardulinkversion] This is the ready ardulink console a
 * complete SWING application to manage an Arduino board. Console has several
 * tabs with all ready arduino components. Each tab is able to do a specific
 * action sending or listening for messages to arduino or from arduino board.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Console extends JFrame implements Linkable {

	private static final long serialVersionUID = -5288916405936436557L;

	private static final Logger logger = LoggerFactory.getLogger(Console.class);

	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private KeyPressController keyControlPanel;
	private ConnectionStatus connectionStatus;

	private transient Link link;

	private final List<Linkable> linkables = new LinkedList<>();

	private ConnectionPanel connectionPanel;

	protected JButton btnConnect = connectButton();
	protected JButton btnDisconnect = disconnectButton();

	private final transient ConnectionListener connectionListener = new ConnectionListener() {

		@Override
		public void connectionLost() {
			setStates(false);
		}

		@Override
		public void reconnected() {
			setStates(true);
		}

		private void setStates(boolean connected) {
			setEnabled(connected);
			btnConnect.setEnabled(!connected);
			btnDisconnect.setEnabled(connected);
		}

	};

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		invokeLater(() -> {
			try {
				setLookAndFeel("Nimbus");
				Console frame = new Console();
				setupExceptionHandler(frame);
				frame.setVisible(true);
			} catch (Exception e) {
				logger.error("Error while processing main method", e);
			}
		});
	}

	private static void setupExceptionHandler(Console console) {
		UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable t) {
				try {
					logger.error("Uncaught Exception", t);
					Throwable rootCause = rootCauseWithMessage(t);
					JOptionPane.showMessageDialog(console,
							rootCause.getClass().getName() + ": " + rootCause.getMessage(), "Error", ERROR_MESSAGE);
				} catch (Throwable t2) {
					/*
					 * don't let the Throwable get thrown out, will cause infinite looping!
					 */
					logger.error("Exception in ExceptionHandler", t2);
				}
			}

			private Throwable rootCauseWithMessage(Throwable throwable) {
				return Streams.getLast(getCauses(throwable).filter(t -> t.getMessage() != null)).orElse(throwable);
			}

		};
		Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
		System.setProperty("sun.awt.exception.handler", exceptionHandler.getClass().getName()); //$NON-NLS-1$
	}

	/**
	 * Create the frame.
	 */
	public Console() {
		setIconImage(icon("icons/logo_icon.png").getImage());
		setTitle("Ardulink Console");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 730, 620);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel configurationPanel = new JPanel();
		tabbedPane.addTab("Configuration", null, configurationPanel, null);
		configurationPanel.setLayout(new BorderLayout(0, 0));

		JPanel connectPanel = new JPanel();
		connectPanel.add(btnConnect);
		connectPanel.add(btnDisconnect);
		configurationPanel.add(connectPanel, BorderLayout.SOUTH);

		JPanel allConnectionsPanel = new JPanel();
		configurationPanel.add(allConnectionsPanel, BorderLayout.CENTER);
		GridBagLayout gbl_allConnectionsPanel = new GridBagLayout();
		allConnectionsPanel.setLayout(gbl_allConnectionsPanel);

		connectionPanel = new ConnectionPanel();
		connectionPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		GridBagConstraints gbcGenericConnectionPanel = new GridBagConstraints();
		gbcGenericConnectionPanel.insets = new Insets(0, 0, 0, 10);
		gbcGenericConnectionPanel.anchor = GridBagConstraints.NORTH;
		gbcGenericConnectionPanel.fill = GridBagConstraints.BOTH;
		gbcGenericConnectionPanel.gridx = 0;
		gbcGenericConnectionPanel.gridy = 1;
		gbcGenericConnectionPanel.weightx = 1;
		gbcGenericConnectionPanel.weighty = 1;
		allConnectionsPanel.add(connectionPanel, gbcGenericConnectionPanel);

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

		JPanel monitorPanel = new JPanel();
		tabbedPane.addTab("Monitor Panel", null, monitorPanel, null);
		monitorPanel.setLayout(new BorderLayout());

		SerialMonitor serialMonitor = new SerialMonitor();
		linkables.add(serialMonitor);
		monitorPanel.add(serialMonitor, BorderLayout.CENTER);

		JPanel stateBar = new JPanel();
		FlowLayout flowLayout1 = (FlowLayout) stateBar.getLayout();
		flowLayout1.setVgap(0);
		flowLayout1.setAlignment(FlowLayout.LEFT);
		contentPane.add(stateBar, BorderLayout.SOUTH);

		connectionStatus = new ConnectionStatus();
		linkables.add(connectionStatus);
		FlowLayout flowLayout2 = (FlowLayout) connectionStatus.getLayout();
		flowLayout2.setVgap(0);
		flowLayout2.setAlignment(FlowLayout.LEFT);
		stateBar.add(connectionStatus, BorderLayout.SOUTH);

		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (tabbedPane.getSelectedComponent().equals(keyControlPanel)) {
					keyControlPanel.requestFocus();
				}
			}
		});

		this.connectionListener.connectionLost();
		setEnabled(false);
		pack();
	}

	private JButton connectButton() {
		JButton button = new JButton("Connect");
		button.addActionListener(event -> {
			try {
				setLink(createLink());
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		});
		return button;
	}

	protected Link createLink() {
		return connectionPanel.createLink();
	}

	private JButton disconnectButton() {
		JButton button = new JButton("Disconnect");
		button.addActionListener(e -> {
			logger.info("Connection status: {}", !link.disconnect());
			setLink(null);
		});
		return button;
	}

	private PWMController pwmController(int pin) {
		PWMController pwmController = new PWMController();
		pwmController.setPin(pin);
		linkables.add(pwmController);
		return pwmController;
	}

	private SimplePositionListener simplePositionListener(ModifiableJoystick joystick) {
		SimplePositionListener positionListener = new SimplePositionListener();
		joystick.addPositionListener(positionListener);
		return positionListener;
	}

	private ModifiableJoystick modifiableJoystick(String id) {
		ModifiableJoystick joystick = new ModifiableJoystick();
		joystick.setId(id);
		linkables.add(joystick);
		return joystick;
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

	@Override
	public void setEnabled(boolean enabled) {
		for (Component component : tabbedPane.getComponents()) {
			setEnabled(enabled, component);
		}
	}

	private void setEnabled(boolean enabled, Component component) {
		if (component == connectionPanel) {
			enabled = !enabled;
		}
		if (component != btnConnect && component != btnDisconnect) {
			if (component instanceof Container) {
				for (Component subComp : ((Container) component).getComponents()) {
					setEnabled(enabled, subComp);
				}
			}
			component.setEnabled(enabled);
		}
	}

	@Override
	public void setLink(Link newLink) {
		if (this.link != null) {
			this.link.removeConnectionListener(this.connectionListener);
		}
		this.link = newLink;
		if (this.link != null) {
			this.link.addConnectionListener(this.connectionListener);
		}
		if (this.link != null) {
			connectionListener.reconnected();
		} else {
			connectionListener.connectionLost();
		}
		callLinkables(this.link);
	}

	public Link getLink() {
		return link;
	}

	private void callLinkables(Link link) {
		for (Linkable linkable : linkables) {
			linkable.setLink(link);
		}
	}

}
