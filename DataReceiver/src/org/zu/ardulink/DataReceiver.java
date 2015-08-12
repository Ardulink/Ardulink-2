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
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DigitalReadChangeEvent;
import org.zu.ardulink.event.DigitalReadChangeListener;
import org.zu.ardulink.event.DisconnectionEvent;

public class DataReceiver implements DigitalReadChangeListener, RawDataListener, ConnectionListener {
	
	private Link link;

	public static void main(String[] args) {
		
		try {
			DataReceiver dataReceiver = new DataReceiver();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public DataReceiver() {
		link = getLink();
		List<String> portList = link.getPortList();
		if(portList != null && portList.size() > 0) {
			
			// Register this class as connection listener
			link.addConnectionListener(this);
			
			String port = portList.get(0);
			System.out.println("Trying to connect to: " + port);
			boolean connected = link.connect(port, 115200);
			if(!connected) {
				throw new RuntimeException("Connection failed!");
			}
			try {
				System.out.println("Wait a while for Arduino boot");
				Thread.sleep(10000); // Wait for a while just to Arduino reboot
				System.out.println("Ok, now it should be ready...");
				link.addDigitalReadChangeListener(this);
				link.addRawDataListener(this);
				
				// link.startListenDigitalPin(2); // Add this if getPinListening() returns DigitalReadChangeListener.ALL_PINS
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1.getCause());
			} 
		} else {
			throw new RuntimeException("No port found!");
		}
	}

	/**
	 * Return the Link used from this example
	 * @return
	 */
	private Link getLink() {
		Link link = Link.getDefaultInstance();

		/*
		 * Decomment these rows to try network connection with Ardulink Proxy Server
		 */
//		try {
//			link = Link.createInstance("network", new NetworkProxyConnection("127.0.0.1", 4478));
//		} catch (IOException e) {
//			throw new RuntimeException(e.getCause());
//		}
		
		return link;
	}

	/**
	 * All messages from Arduino are sent to this method in their raw format
	 */
	@Override
	public void parseInput(String id, int numBytes, int[] message) {
		
		System.out.println("Message from: " + id);
		StringBuilder builder = new StringBuilder(numBytes);
		for (int i = 0; i < numBytes; i++) {
			builder.append((char)message[i]);
		}
		
		System.out.println("Message: " + builder.toString());	
	}

	/**
	 * When a PIN change its state this method is invoked
	 */
	@Override
	public void stateChanged(DigitalReadChangeEvent e) {
		
		System.out.println("PIN state changed. PIN: " + e.getPin() + " Value: " + e.getValue());		
	}

	/**
	 * This method set which PIN this listener is listening for use DigitalReadChangeListener.ALL_PINS for all PINs
	 */
	@Override
	public int getPinListening() {
		return 2;
	}

	/**
	 * This method is called when a Link is connected to an Arduino
	 */
	@Override
	public void connected(ConnectionEvent e) {
		System.out.println("Connected! Port: " + e.getPortName() + " ID: " + e.getConnectionId());
	}

	/**
	 * This method is called when a Link is disconnected from an Arduino
	 */
	@Override
	public void disconnected(DisconnectionEvent e) {
		System.out.println("Disconnected! ID: " + e.getConnectionId());
	}

}
