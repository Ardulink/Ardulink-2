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
package org.ardulink.gui.serial;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ardulink.util.Throwables.propagate;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.ardulink.core.Connection.Listener;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.qos.QosLink;
import org.ardulink.gui.Linkable;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * This class shows serial incoming messages if the Link is a
 * ConnectionBasedLink.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class SerialMonitor extends JPanel implements Linkable {

	private static final long serialVersionUID = -3662905914867077959L;
	private JTextArea sentTextArea;
	private JTextArea receivedTextArea;
	private JTextField messageTextField;
	private transient ConnectionBasedLink link;
	private final transient Listener listener = new Listener() {

		@Override
		public void received(byte[] bytes) throws IOException {
			receivedTextArea.append("\n" + new String(bytes, UTF_8));
		}

		@Override
		public void sent(byte[] bytes) throws IOException {
			sentTextArea.append(new String(bytes, UTF_8));
		}

	};

	/**
	 * Create the panel.
	 */
	public SerialMonitor() {
		setPreferredSize(new Dimension(640, 315));
		setLayout(new BorderLayout(0, 0));

		JPanel sendPanel = new JPanel();
		add(sendPanel, BorderLayout.NORTH);
		sendPanel.setLayout(new BorderLayout(0, 0));

		messageTextField = new JTextField();
		sendPanel.add(messageTextField, BorderLayout.CENTER);
		messageTextField.setColumns(10);

		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(__ -> {
			try {
				link.getConnection().write((messageTextField.getText() + "\n").getBytes());
			} catch (IOException ex) {
				throw propagate(ex);
			}
		});
		sendPanel.add(sendButton, BorderLayout.EAST);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);

		JPanel sentPanel = new JPanel();
		sentPanel.setPreferredSize(new Dimension(640, 200));
		splitPane.setLeftComponent(sentPanel);
		sentPanel.setLayout(new BorderLayout(0, 0));

		JLabel sentLabel = new JLabel("Sent:");
		sentPanel.add(sentLabel, BorderLayout.NORTH);

		JScrollPane sentScrollPane = new JScrollPane();
		sentPanel.add(sentScrollPane, BorderLayout.CENTER);

		sentTextArea = new JTextArea();
		sentTextArea.setEditable(false);
		sentScrollPane.setViewportView(sentTextArea);

		JPanel receivedPanel = new JPanel();
		receivedPanel.setPreferredSize(new Dimension(640, 200));
		splitPane.setRightComponent(receivedPanel);
		receivedPanel.setLayout(new BorderLayout(0, 0));

		JLabel receivedLabel = new JLabel("Received:");
		receivedPanel.add(receivedLabel, BorderLayout.NORTH);

		JScrollPane receivedScrollPane = new JScrollPane();
		receivedPanel.add(receivedScrollPane, BorderLayout.CENTER);

		receivedTextArea = new JTextArea();
		receivedTextArea.setEditable(false);
		receivedScrollPane.setViewportView(receivedTextArea);

		splitPane.setDividerLocation(0.5);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(buttonPanel, BorderLayout.SOUTH);

		JButton clearSentButton = new JButton("Clear Sent");
		clearSentButton.addActionListener(__ -> sentTextArea.setText(""));
		buttonPanel.add(clearSentButton);

		JButton clearReceivedButton = new JButton("Clear Received");
		clearReceivedButton.addActionListener(__ -> receivedTextArea.setText(""));
		buttonPanel.add(clearReceivedButton);
	}

	@Override
	public void setLink(Link link) {
		if (this.link != null) {
			this.link.getConnection().removeListener(listener);
		}
		link = link instanceof QosLink ? ((QosLink) link).getDelegate() : link;

		this.link = null;
		if (link instanceof ConnectionBasedLink) {
			this.link = (ConnectionBasedLink) link;
			this.link.getConnection().addListener(listener);
		}
		sentTextArea.setText("");
		receivedTextArea.setText("");
	}

}