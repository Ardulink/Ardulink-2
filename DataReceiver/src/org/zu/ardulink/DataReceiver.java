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

package org.zu.ardulink;

import java.io.IOException;
import java.util.List;

import org.zu.ardulink.connection.proxy.NetworkProxyConnection;
import org.zu.ardulink.connection.proxy.NetworkProxyServer;
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DigitalReadChangeEvent;
import org.zu.ardulink.event.DigitalReadChangeListener;
import org.zu.ardulink.event.DisconnectionEvent;

public class DataReceiver {

	private String remote;

	private Link link;

	public static void main(String[] args) {
		new DataReceiver(args);
	}

	public DataReceiver(String[] args) {
		if (args.length > 0) {
			remote = args[0];
		}
		link = createLink();
		List<String> portList = link.getPortList();
		if (portList != null && !portList.isEmpty()) {

			// Register this class as connection listener
			link.addConnectionListener(connectionListener());

			String port = portList.get(0);
			System.out.println("Trying to connect to: " + port);
			boolean connected = link.connect(port, 115200);
			if (!connected) {
				throw new RuntimeException("Connection failed!");
			}
			try {
				System.out.println("Wait a while for Arduino boot");
				Thread.sleep(10000); // Wait for a while just to Arduino reboot
				System.out.println("Ok, now it should be ready...");
				DigitalReadChangeListener drcl = digitalReadChangeListener(2);
				link.addDigitalReadChangeListener(drcl);
				link.addRawDataListener(rawDataListener());

				if (drcl.getPinListening() == DigitalReadChangeListener.ALL_PINS) {
					link.startListenDigitalPin(2);
				}
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1);
			}
		} else {
			throw new RuntimeException("No port found!");
		}
	}

	private ConnectionListener connectionListener() {
		return new ConnectionListener() {

			/**
			 * This method is called when a Link is connected to an Arduino
			 */
			@Override
			public void connected(ConnectionEvent e) {
				System.out.println("Connected! Port: " + e.getPortName()
						+ " ID: " + e.getConnectionId());
			}

			/**
			 * This method is called when a Link is disconnected from an
			 * Arduino.
			 */
			@Override
			public void disconnected(DisconnectionEvent e) {
				System.out.println("Disconnected! ID: " + e.getConnectionId());
			}

		};
	}

	private RawDataListener rawDataListener() {
		return new RawDataListener() {

			/**
			 * All messages from Arduino are sent to this method in their raw
			 * format
			 */
			@Override
			public void parseInput(String id, int numBytes, int[] message) {

				System.out.println("Message from: " + id);
				StringBuilder builder = new StringBuilder(numBytes);
				for (int i = 0; i < numBytes; i++) {
					builder.append((char) message[i]);
				}

				System.out.println("Message: " + builder.toString());
			}

		};
	}

	private DigitalReadChangeListener digitalReadChangeListener(final int pin) {
		return new DigitalReadChangeListener() {

			/**
			 * When a PIN change its state this method is invoked
			 */
			@Override
			public void stateChanged(DigitalReadChangeEvent e) {
				System.out.println("PIN state changed. PIN: " + e.getPin()
						+ " Value: " + e.getValue());
			}

			/**
			 * This method set which PIN this listener is listening for use
			 * DigitalReadChangeListener.ALL_PINS for all PINs
			 */
			@Override
			public int getPinListening() {
				return pin;
			}

		};
	}

	/**
	 * Return the Link used from this example
	 * 
	 * @return
	 */
	private Link createLink() {
		if (remote == null || remote.isEmpty()) {
			return Link.getDefaultInstance();
		}

		String[] hostAndPort = remote.split("\\:");
		try {
			int port = hostAndPort.length == 1 ? NetworkProxyServer.DEFAULT_LISTENING_PORT
					: Integer.parseInt(hostAndPort[1]);
			return Link.createInstance("network", new NetworkProxyConnection(
					hostAndPort[0], port));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
