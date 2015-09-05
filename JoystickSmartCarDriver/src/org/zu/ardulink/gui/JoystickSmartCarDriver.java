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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.zu.ardulink.Link;
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DisconnectionEvent;
import org.zu.ardulink.gui.customcomponents.joystick.ModifiableJoystick;
import org.zu.ardulink.protocol.ReplyMessageCallback;

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;

public class JoystickSmartCarDriver extends JFrame implements ConnectionListener, Linkable {

	private static final long serialVersionUID = 1402473246181814940L;

	private JPanel contentPane;
	private Link link = null;
	private List<Linkable> linkables = new LinkedList<Linkable>();
	
	private BluetoothConnectionPanel bluetoothConnectionPanel;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private JPanel controlPanel;
	private ModifiableJoystick joystick;
	private MotorDriver motorDriver = new MotorDriver();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(NimbusLookAndFeel.class.getCanonicalName());
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
			public void actionPerformed(ActionEvent e) {
				if(link != null) {
					String deviceName = bluetoothConnectionPanel.getSelectedDevice();
					link.connect(deviceName);
				}
			}
		});
		connectionPanel.add(btnConnect);
		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(link != null) {
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
		// not use Joystick link, PositionEvents will be captured and managed with a specifica class
		joystick.setLink(null);
		joystick.setId("joy");
		joystick.addPositionListener(motorDriver);
		controlPanel.add(joystick, BorderLayout.CENTER);
		
		linkables.add(motorDriver);
		
		setLink(bluetoothConnectionPanel.getLink());
	}

	@Override
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
	public ReplyMessageCallback getReplyMessageCallback() {
		throw new RuntimeException("Not developed yet");
	}

	@Override
	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		throw new RuntimeException("Not developed yet");
	}

	@Override
	public void connected(ConnectionEvent e) {
		bluetoothConnectionPanel.setEnabled(false);
		btnConnect.setEnabled(false);
		btnDisconnect.setEnabled(true);
	}

	@Override
	public void disconnected(DisconnectionEvent e) {
		bluetoothConnectionPanel.setEnabled(true);
		btnConnect.setEnabled(true);
		btnDisconnect.setEnabled(false);
	}
}
