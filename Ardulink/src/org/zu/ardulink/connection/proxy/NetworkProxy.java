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

package org.zu.ardulink.connection.proxy;

import gnu.io.net.Network_iface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.zu.ardulink.Link;
import org.zu.ardulink.connection.Connection;

public class NetworkProxy implements Connection, NetworkProxyMessages {

	private Socket socket = null;

	private BufferedReader bufferedReader;
	private InputStream inputStream;
	private PrintWriter printWriter;
	private OutputStream outputStream;
	
	private boolean handshakeComplete = false;
	
	private Network_iface contact;
	
	private String id;
	
	public NetworkProxy(String host, int port) throws IOException {
		id = host + ":" + port;
		socket = new Socket(host, port);

		inputStream = socket.getInputStream();
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		outputStream = socket.getOutputStream();
		printWriter = new PrintWriter(outputStream, true);
	}

	@Override
	public List<String> getPortList() {
		List<String> retvalue = null;
		if(!handshakeComplete) {
			try {
				printWriter.println(GET_PORT_LIST_CMD);
				printWriter.flush();
				String inputLine = bufferedReader.readLine();
				if(inputLine != null && inputLine.startsWith(NUMBER_OF_PORTS)) {
					int numOfPorts = Integer.parseInt(inputLine.substring(NUMBER_OF_PORTS.length()));
					retvalue = new ArrayList<String>(numOfPorts);
					for(int i = 0; i < numOfPorts; i++) {
						inputLine = bufferedReader.readLine();
						retvalue.add(inputLine);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return retvalue;
	}

	@Override
	public boolean connect(Object... params) {
		String portName = null;
		Integer baudRate = Link.DEFAULT_BAUDRATE;
		if(params == null || params.length < 1) {
			throw new RuntimeException("This connection accepts a String port name and a Integer baud rate. Only the port name is mandatory. Null or zero arguments passed.");
		}
		if(!(params[0] instanceof String)) {
			throw new RuntimeException("This connection accepts a String port name and a Integer baud rate. Only the port name is mandatory. First argument was not a String");
		} else {
			portName =(String)params[0]; 
		}
		if(params.length > 1 && !(params[1] instanceof Integer)) {
			throw new RuntimeException("This connection accepts a String port name and a Integer baud rate. Only the port name is mandatory. Second argument was not an Integer");
		} else {
			baudRate = (Integer)params[1];
		}
		
		boolean retvalue = false;
		try {
			printWriter.println(CONNECT_CMD);
			printWriter.println(portName);
			printWriter.println(baudRate.toString());
			printWriter.flush();
			
			String inputLine = bufferedReader.readLine();
			if(inputLine != null && inputLine.equals(OK)) {
				retvalue = true;
				handshakeComplete = true;
			}
			contact.networkConnected(id, portName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retvalue;
	}

	@Override
	public boolean disconnect() {
		try {
			socket.close();
			contact.networkDisconnected(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return socket.isConnected();
	}

	@Override
	public boolean isConnected() {
		boolean retvalue = socket.isConnected() & handshakeComplete;
		return retvalue;
	}

	@Override
	public boolean writeSerial(String message) {
		printWriter.print(message);
		printWriter.flush();
			
		return true;
	}

	@Override
	public boolean writeSerial(int numBytes, int[] message) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void setConnectionContact(Network_iface contact) {
		this.contact = contact;
	}

	public boolean isHandshakeComplete() {
		return handshakeComplete;
	}
}
