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
import static java.util.function.Predicate.not;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static org.ardulink.core.NullLink.NULL_LINK;
import static org.ardulink.core.NullLink.isNullLink;
import static org.ardulink.gui.Icons.icon;
import static org.ardulink.gui.facility.LAFUtil.setLookAndFeel;
import static org.ardulink.gui.util.SwingUtilities.componentsStream;
import static org.ardulink.util.Predicates.attribute;
import static org.ardulink.util.Streams.getLast;
import static org.ardulink.util.Throwables.getCauses;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.ConnectionListener;
import org.ardulink.core.Link;
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
import org.ardulink.gui.statestore.StateStore;
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

	private final StateStore stateStore;
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private KeyPressController keyControlPanel;
	private ConnectionStatus connectionStatus;

	private transient Link link;

	private ConnectionPanel connectionPanel;

	private JButton btnConnect = connectButton();
	private JButton btnDisconnect = disconnectButton();
	private final ConnectionListener connectionListener = new ConnectionListener() {

		@Override
		public void reconnected() {
			setEnabled(true);
			btnConnect.setEnabled(false);
			btnDisconnect.setEnabled(true);
		}

		@Override
		public void connectionLost() {
			setEnabled(false);
			btnConnect.setEnabled(true);
			btnDisconnect.setEnabled(false);
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
				return getLast(getCauses(throwable) //
						.filter(attribute(Throwable::getMessage, Objects::nonNull))) //
						.orElse(throwable);
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
		GridBagLayout gblAllConnectionsPanel = new GridBagLayout();
		allConnectionsPanel.setLayout(gblAllConnectionsPanel);

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
		tabbedPane.addTab("Key Control", null, keyControlPanel, null);

		JPanel powerPanel = new JPanel();
		tabbedPane.addTab("Power Panel", null, powerPanel, null);
		powerPanel.setLayout(new GridLayout(2, 3, 0, 0));

		for (int pin = 3; pin <= 11; pin++) {
			powerPanel.add(new PWMController().setPin(pin));
		}

		JPanel switchPanel = new JPanel();
		tabbedPane.addTab("Switch Panel", null, switchPanel, null);
		switchPanel.setLayout(new GridLayout(5, 3, 0, 0));

		for (int pin = 3; pin <= 13; pin++) {
			switchPanel.add(new SwitchController().setPin(pin));
		}

		JPanel joystickPanel = new JPanel();
		tabbedPane.addTab("Joystick Panel", null, joystickPanel, null);
		joystickPanel.setLayout(new GridLayout(2, 2, 0, 0));
		
		for (String id : List.of("Left", "Right")) {
			ModifiableJoystick joy = new ModifiableJoystick().setId(id);
			joystickPanel.add(joy);
			joystickPanel.add(simplePositionListener(joy));
		}

		JPanel sensorDigitalPanel = new JPanel();
		tabbedPane.addTab("Digital Sensor Panel", null, sensorDigitalPanel, null);
		sensorDigitalPanel.setLayout(new GridLayout(4, 3, 0, 0));

		for (int pin = 2; pin <= 12; pin++) {
			sensorDigitalPanel.add(new DigitalPinStatus().setPin(pin));
		}

		JPanel sensorAnalogPanel = new JPanel();
		sensorAnalogPanel.setLayout(new GridLayout(2, 3, 0, 0));
		tabbedPane.addTab("Analog Sensor Panel", null, sensorAnalogPanel, null);

		for (int pin = 0; pin <= 5; pin++) {
			sensorAnalogPanel.add(new AnalogPinStatus().setPin(pin));
		}

		JPanel customPanel = new JPanel();
		tabbedPane.addTab("Custom Components", null, customPanel, null);
		customPanel.setLayout(new GridLayout(2, 3, 10, 15));

		for (int i = 0; i <= 2; i++) {
			customPanel.add(new ModifiableSignalButton());
		}

		for (int i = 0; i <= 2; i++) {
			customPanel.add(new ModifiableToggleSignalButton());
		}

		JPanel tonePanel = new JPanel();
		tabbedPane.addTab("Tone Panel", null, tonePanel, null);

		tonePanel.add(new ToneController());

		JPanel rgbPanel = new JPanel();
		tabbedPane.addTab("RGB Panel", null, rgbPanel, null);

		rgbPanel.add(new RGBController());

		JPanel monitorPanel = new JPanel();
		tabbedPane.addTab("Monitor Panel", null, monitorPanel, null);
		monitorPanel.setLayout(new BorderLayout());

		monitorPanel.add(new SerialMonitor(), BorderLayout.CENTER);

		JPanel stateBar = new JPanel();
		FlowLayout flowLayout1 = (FlowLayout) stateBar.getLayout();
		flowLayout1.setVgap(0);
		flowLayout1.setAlignment(FlowLayout.LEFT);
		contentPane.add(stateBar, BorderLayout.SOUTH);

		connectionStatus = new ConnectionStatus();
		FlowLayout flowLayout2 = (FlowLayout) connectionStatus.getLayout();
		flowLayout2.setVgap(0);
		flowLayout2.setAlignment(FlowLayout.LEFT);
		stateBar.add(connectionStatus, BorderLayout.SOUTH);

		tabbedPane.addChangeListener(__ -> {
			if (tabbedPane.getSelectedComponent().equals(keyControlPanel)) {
				keyControlPanel.requestFocus();
			}
		});

		pack();
		stateStore = new StateStore(contentPane).snapshot().removeStates(allConnectionsPanel, configurationPanel,
				connectionStatus);
		setLink(NULL_LINK);
	}

	private JButton connectButton() {
		JButton button = new JButton("Connect");
		button.addActionListener(__ -> {
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
		button.addActionListener(__ -> {
			try {
				link.close();
				logger.info("Connection closed");
			} catch (IOException ex) {
				throw Throwables.propagate(ex);
			}
			setLink(NULL_LINK);
		});
		return button;
	}

	private SimplePositionListener simplePositionListener(ModifiableJoystick joystick) {
		SimplePositionListener positionListener = new SimplePositionListener();
		joystick.addPositionListener(positionListener);
		return positionListener;
	}

	@Override
	public void setEnabled(boolean enabled) {
		for (Component component : tabbedPane.getComponents()) {
			setEnabled(enabled, component);
		}
	}

	private void setEnabled(boolean enabled, Component component) {
		if (component != btnConnect && component != btnDisconnect) {
			enabled ^= (component == connectionPanel);
			if (component instanceof Container) {
				for (Component subComp : ((Container) component).getComponents()) {
					setEnabled(enabled, subComp);
				}
			}
			component.setEnabled(enabled);
		}
	}

	@Override
	public void setLink(Link link) {
		if (this.link instanceof ConnectionBasedLink) {
			((ConnectionBasedLink) this.link).removeConnectionListener(connectionListener);
		}
		this.link = link;
		if (this.link instanceof ConnectionBasedLink) {
			((ConnectionBasedLink) this.link).addConnectionListener(connectionListener);
		}
		componentsStream(this) //
				.filter(not(this::equals)) //
				.filter(Linkable.class::isInstance) //
				.map(Linkable.class::cast) //
				.forEach(l -> l.setLink(this.link));
		stateStore.restore();
		if (isNullLink(this.link)) {
			connectionListener.connectionLost();
		} else {
			connectionListener.reconnected();
		}
	}

	public Link getLink() {
		return link;
	}

}
