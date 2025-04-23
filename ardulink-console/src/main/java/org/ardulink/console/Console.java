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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.EventQueue.invokeLater;
import static java.awt.FlowLayout.LEFT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NORTH;
import static java.util.function.Predicate.not;
import static java.util.stream.IntStream.rangeClosed;
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
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
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
			public void uncaughtException(Thread thread, Throwable throwable) {
				try {
					logger.error("Uncaught Exception", throwable);
					Throwable rootCause = rootCauseWithMessage(throwable);
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
		System.setProperty("sun.awt.exception.handler", exceptionHandler.getClass().getName());
	}

	/**
	 * Create the frame.
	 */
	public Console() {
		setIconImage(icon("icons/logo_icon.png").getImage());
		setTitle("Ardulink Console");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(100, 100, 730, 620);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(tabbedPane, CENTER);

		JPanel configurationPanel = new JPanel();
		tabbedPane.addTab("Configuration", null, configurationPanel, null);
		configurationPanel.setLayout(new BorderLayout(0, 0));

		JPanel connectPanel = new JPanel();
		connectPanel.add(btnConnect);
		connectPanel.add(btnDisconnect);
		configurationPanel.add(connectPanel, SOUTH);

		JPanel allConnectionsPanel = new JPanel();
		configurationPanel.add(allConnectionsPanel, CENTER);
		GridBagLayout gblAllConnectionsPanel = new GridBagLayout();
		allConnectionsPanel.setLayout(gblAllConnectionsPanel);

		connectionPanel = new ConnectionPanel();
		connectionPanel.setAlignmentY(BOTTOM_ALIGNMENT);
		GridBagConstraints gbcGenericConnectionPanel = new GridBagConstraints();
		gbcGenericConnectionPanel.insets = new Insets(0, 0, 0, 10);
		gbcGenericConnectionPanel.anchor = NORTH;
		gbcGenericConnectionPanel.fill = BOTH;
		gbcGenericConnectionPanel.gridx = 0;
		gbcGenericConnectionPanel.gridy = 1;
		gbcGenericConnectionPanel.weightx = 1;
		gbcGenericConnectionPanel.weighty = 1;
		allConnectionsPanel.add(connectionPanel, gbcGenericConnectionPanel);

		keyControlPanel = new KeyPressController();
		tabbedPane.addTab("Key Control", null, keyControlPanel, null);

		JPanel powerPanel = new JPanel();
		powerPanel.setLayout(new GridLayout(2, 3, 0, 0));
		tabbedPane.addTab("Power Panel", null, powerPanel, null);

		addMulti(3, 11, pin -> new PWMController().setPin(pin), powerPanel);

		JPanel switchPanel = new JPanel();
		switchPanel.setLayout(new GridLayout(5, 3, 0, 0));
		tabbedPane.addTab("Switch Panel", null, switchPanel, null);

		addMulti(3, 13, pin -> new SwitchController().setPin(pin), switchPanel);

		JPanel joystickPanel = new JPanel();
		joystickPanel.setLayout(new GridLayout(2, 2, 0, 0));
		tabbedPane.addTab("Joystick Panel", null, joystickPanel, null);

		Stream.of("Left", "Right").map(id -> {
			ModifiableJoystick joy = new ModifiableJoystick().setId(id);
			joystickPanel.add(joy);
			SimplePositionListener listener = new SimplePositionListener();
			joy.addPositionListener(listener);
			return joy;
		}).forEach(joystickPanel::add);

		JPanel sensorDigitalPanel = new JPanel();
		tabbedPane.addTab("Digital Sensor Panel", null, sensorDigitalPanel, null);
		sensorDigitalPanel.setLayout(new GridLayout(4, 3, 0, 0));

		addMulti(2, 12, pin -> new DigitalPinStatus().setPin(pin), sensorDigitalPanel);

		JPanel sensorAnalogPanel = new JPanel();
		sensorAnalogPanel.setLayout(new GridLayout(2, 3, 0, 0));
		tabbedPane.addTab("Analog Sensor Panel", null, sensorAnalogPanel, null);

		addMulti(0, 5, pin -> new AnalogPinStatus().setPin(pin), sensorAnalogPanel);

		JPanel customPanel = new JPanel();
		tabbedPane.addTab("Custom Components", null, customPanel, null);
		customPanel.setLayout(new GridLayout(2, 3, 10, 15));

		addMulti(0, 2, __ -> new ModifiableSignalButton(), customPanel);

		addMulti(0, 2, __ -> new ModifiableToggleSignalButton(), customPanel);

		JPanel tonePanel = new JPanel();
		tabbedPane.addTab("Tone Panel", null, tonePanel, null);

		tonePanel.add(new ToneController());

		JPanel rgbPanel = new JPanel();
		tabbedPane.addTab("RGB Panel", null, rgbPanel, null);

		rgbPanel.add(new RGBController());

		JPanel monitorPanel = new JPanel();
		tabbedPane.addTab("Monitor Panel", null, monitorPanel, null);
		monitorPanel.setLayout(new BorderLayout());

		monitorPanel.add(new SerialMonitor(), CENTER);

		JPanel stateBar = new JPanel();
		stateBar.setLayout(newFlowLayout());
		contentPane.add(stateBar, SOUTH);

		connectionStatus = new ConnectionStatus();
		connectionStatus.setLayout(newFlowLayout());
		stateBar.add(connectionStatus, SOUTH);

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

	private static FlowLayout newFlowLayout() {
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setVgap(0);
		flowLayout.setAlignment(LEFT);
		return flowLayout;
	}

	private static void addMulti(int from, int to, IntFunction<JComponent> supplier, JComponent addTo) {
		rangeClosed(from, to).mapToObj(supplier).forEach(addTo::add);
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
