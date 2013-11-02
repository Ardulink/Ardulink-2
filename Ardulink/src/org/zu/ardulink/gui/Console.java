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
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.zu.ardulink.Link;

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;

public class Console extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5288916405936436557L;
	
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private JPanel keyControlPanel;
	private ConnectionPanel parameterConnectionPanel;
	
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
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String comPort = parameterConnectionPanel.getConnectionPort();
				String baudRateS = parameterConnectionPanel.getBaudRate();
				
				if(comPort == null || "".equals(comPort)) {
					JOptionPane.showMessageDialog(parameterConnectionPanel, "Invalid COM PORT setted.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if(baudRateS == null || "".equals(baudRateS)) {
					JOptionPane.showMessageDialog(parameterConnectionPanel, "Invalid baud rate setted. Advice: set " + Link.DEFAULT_BAUDRATE, "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						int baudRate = Integer.parseInt(baudRateS);
						
						boolean connected = Link.getDefaultInstance().connect(comPort, baudRate);
						logger.info("Connection status: " + connected);
					}
					catch(Exception ex) {
						JOptionPane.showMessageDialog(parameterConnectionPanel, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		connectPanel.add(btnConnect);
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean connected = !Link.getDefaultInstance().disconnect();
				logger.info("Connection status: " + connected);
			}
		});
		connectPanel.add(btnDisconnect);
		
		parameterConnectionPanel = new ConnectionPanel();
		configurationPanel.add(parameterConnectionPanel, BorderLayout.CENTER);
		parameterConnectionPanel.setLayout(null);
		
		keyControlPanel = new KeyPressController();
		tabbedPane.addTab("Key Control", null, keyControlPanel, null);
				
		JPanel powerPanel = new JPanel();
		tabbedPane.addTab("Power Panel", null, powerPanel, null);
		powerPanel.setLayout(new GridLayout(2, 3, 0, 0));
		
		PWMController panelPin3 = new PWMController();
		panelPin3.setPin(3);
		powerPanel.add(panelPin3);
		
		PWMController panelPin5 = new PWMController();
		panelPin5.setPin(5);
		powerPanel.add(panelPin5);
		
		PWMController panelPin6 = new PWMController();
		panelPin6.setPin(6);
		powerPanel.add(panelPin6);
		
		PWMController panelPin9 = new PWMController();
		panelPin9.setPin(9);
		powerPanel.add(panelPin9);
		
		PWMController panelPin10 = new PWMController();
		panelPin10.setPin(10);
		powerPanel.add(panelPin10);
		
		PWMController panelPin11 = new PWMController();
		panelPin11.setPin(11);
		powerPanel.add(panelPin11);
				
		JPanel switchPanel = new JPanel();
		tabbedPane.addTab("Switch Panel", null, switchPanel, null);
		switchPanel.setLayout(new GridLayout(5, 3, 0, 0));
				
		SwitchController switchController3 = new SwitchController();
		switchController3.setPin(3);
		switchPanel.add(switchController3);
		
		SwitchController switchController4 = new SwitchController();
		switchController4.setPin(4);
		switchPanel.add(switchController4);
		
		SwitchController switchController5 = new SwitchController();
		switchController5.setPin(5);
		switchPanel.add(switchController5);
		
		SwitchController switchController6 = new SwitchController();
		switchController6.setPin(6);
		switchPanel.add(switchController6);
		
		SwitchController switchController7 = new SwitchController();
		switchController7.setPin(7);
		switchPanel.add(switchController7);
		
		SwitchController switchController8 = new SwitchController();
		switchController8.setPin(8);
		switchPanel.add(switchController8);
		
		SwitchController switchController9 = new SwitchController();
		switchController9.setPin(9);
		switchPanel.add(switchController9);
		
		SwitchController switchController10 = new SwitchController();
		switchController10.setPin(10);
		switchPanel.add(switchController10);
		
		SwitchController switchController11 = new SwitchController();
		switchController11.setPin(11);
		switchPanel.add(switchController11);
		
		SwitchController switchController12 = new SwitchController();
		switchController12.setPin(12);
		switchPanel.add(switchController12);
		
		SwitchController switchController13 = new SwitchController();
		switchController13.setPin(13);
		switchPanel.add(switchController13);
		
		JPanel sensorDigitalPanel = new JPanel();
		tabbedPane.addTab("Sensor Digital Panel", null, sensorDigitalPanel, null);
		sensorDigitalPanel.setLayout(new GridLayout(4, 3, 0, 0));
		
		DigitalPinStatus digitalPinStatus2 = new DigitalPinStatus();
		digitalPinStatus2.setPin(2);
		sensorDigitalPanel.add(digitalPinStatus2);
		
		DigitalPinStatus digitalPinStatus3 = new DigitalPinStatus();
		digitalPinStatus3.setPin(3);
		sensorDigitalPanel.add(digitalPinStatus3);
		
		DigitalPinStatus digitalPinStatus4 = new DigitalPinStatus();
		digitalPinStatus4.setPin(4);
		sensorDigitalPanel.add(digitalPinStatus4);
		
		DigitalPinStatus digitalPinStatus5 = new DigitalPinStatus();
		digitalPinStatus5.setPin(5);
		sensorDigitalPanel.add(digitalPinStatus5);
		
		DigitalPinStatus digitalPinStatus6 = new DigitalPinStatus();
		digitalPinStatus6.setPin(6);
		sensorDigitalPanel.add(digitalPinStatus6);
		
		DigitalPinStatus digitalPinStatus7 = new DigitalPinStatus();
		digitalPinStatus7.setPin(7);
		sensorDigitalPanel.add(digitalPinStatus7);
		
		DigitalPinStatus digitalPinStatus8 = new DigitalPinStatus();
		digitalPinStatus8.setPin(8);
		sensorDigitalPanel.add(digitalPinStatus8);
		
		DigitalPinStatus digitalPinStatus9 = new DigitalPinStatus();
		digitalPinStatus9.setPin(9);
		sensorDigitalPanel.add(digitalPinStatus9);
		
		DigitalPinStatus digitalPinStatus10 = new DigitalPinStatus();
		digitalPinStatus10.setPin(10);
		sensorDigitalPanel.add(digitalPinStatus10);
		
		DigitalPinStatus digitalPinStatus11 = new DigitalPinStatus();
		digitalPinStatus11.setPin(11);
		sensorDigitalPanel.add(digitalPinStatus11);
		
		DigitalPinStatus digitalPinStatus12 = new DigitalPinStatus();
		digitalPinStatus12.setPin(12);
		sensorDigitalPanel.add(digitalPinStatus12);
		
		DigitalPinStatus digitalPinStatus13 = new DigitalPinStatus();
		digitalPinStatus13.setPin(13);
		sensorDigitalPanel.add(digitalPinStatus13);
		
		JPanel stateBar = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) stateBar.getLayout();
		flowLayout_1.setVgap(0);
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		contentPane.add(stateBar, BorderLayout.SOUTH);

		JPanel connectionStatus = new ConnectionStatus();
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

	}
}
