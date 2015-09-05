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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.zu.ardulink.Link;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * This component is Able to search for serial ports connected to the Arduino and select one.
 * You can also specify a baud rate.
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * @deprecated use SerialConnectionPanel instead
 * 
 * [adsense]
 *
 */
@Deprecated
public class ConnectionPanel extends JPanel implements Linkable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1290277902714226253L;
	private JTextField connectionPortTextField;
	private JTextField baudRateTextField;
	private JList connectionPortList;
	private JButton discoverButton;

	private Link link = Link.getDefaultInstance();
	
	/**
	 * Create the panel.
	 */
	public ConnectionPanel() {
		Dimension dimension = new Dimension(240, 275);
		setPreferredSize(dimension);
		setMinimumSize(dimension);
		setLayout(null);
		
		JLabel connectionPortLabel = new JLabel("Connection Port:");
		connectionPortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		connectionPortLabel.setBounds(6, 16, 91, 16);
		add(connectionPortLabel);
		
		connectionPortTextField = new JTextField();
		connectionPortTextField.setBounds(108, 10, 122, 28);
		add(connectionPortTextField);
		connectionPortTextField.setColumns(10);
		
		JLabel lblBaudRate = new JLabel("Baud Rate:");
		lblBaudRate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBaudRate.setBounds(6, 44, 91, 16);
		add(lblBaudRate);
		
		baudRateTextField = new JTextField();
		baudRateTextField.setText("" + Link.DEFAULT_BAUDRATE);
		baudRateTextField.setColumns(10);
		baudRateTextField.setBounds(108, 38, 122, 28);
		add(baudRateTextField);
		
		JLabel lblDiscoveredPorts = new JLabel("Discovered Ports:");
		lblDiscoveredPorts.setBounds(6, 87, 101, 16);
		add(lblDiscoveredPorts);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 105, 190, 160);
		add(scrollPane);
		
		connectionPortList = new JList();
		connectionPortList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String connectionPort = (String)connectionPortList.getSelectedValue();
				connectionPortTextField.setText(connectionPort);
			}
		});
		connectionPortList.setLocation(0, -15);
		scrollPane.setViewportView(connectionPortList);
		connectionPortList.setVisibleRowCount(-1);
		connectionPortList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		discoverButton = new JButton("");
		discoverButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> portList = link.getPortList();
//				portList = new ArrayList<String>(); // Mock code...
//				portList.add("COM19");
//				portList.add("COM20");
				if(portList != null && portList.size() > 0) {
					connectionPortList.setModel(new PortListModel(portList));
				}
			}
		});
		discoverButton.setIcon(new ImageIcon(ConnectionPanel.class.getResource("/org/zu/ardulink/gui/icons/search_icon.png")));
		discoverButton.setToolTipText("Discover");
		discoverButton.setBounds(198, 105, 32, 32);
		add(discoverButton);
		

	}
	
	public class PortListModel extends AbstractListModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7316872587399489264L;
		private String[] values = null;
		
		public PortListModel(String[] values) {
			super();
			this.values = values;
		}
		public PortListModel(List<String> values) {
			super();
			this.values = values.toArray(new String[0]);
		}
		
		public int getSize() {
			return values.length;
		}
		public Object getElementAt(int index) {
			return values[index];
		}
	}

	/**
	 * @return the connection port name selected or set
	 */
	public String getConnectionPort() {
		return connectionPortTextField.getText();
	}

	/**
	 * @return the baud rate set
	 */
	//TODO if not numeric take default from Link class.
	public String getBaudRate() {
		return baudRateTextField.getText();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		connectionPortTextField.setEnabled(enabled);
		baudRateTextField.setEnabled(enabled);
		connectionPortList.setEnabled(enabled);
		discoverButton.setEnabled(enabled);
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public ReplyMessageCallback getReplyMessageCallback() {
		throw new RuntimeException("Not developed yet");
	}

	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		throw new RuntimeException("Not developed yet");
	}	
}
