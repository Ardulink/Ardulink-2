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
import org.zu.ardulink.connection.ConnectionContact;
import org.zu.ardulink.protocol.IProtocol;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class NetworkProxyConnection implements Connection, NetworkProxyMessages {

	private Socket socket = null;

	private BufferedReader bufferedReader;
	private InputStream inputStream;
	private PrintWriter printWriter;
	private OutputStream outputStream;
	
	private boolean handshakeComplete = false;
	
	private ConnectionContact contact;
	
	private String id;

	/**
	 * The Thread used to receive the data from the Serial interface.
	 */
	private Thread reader;

	/**
	 * Communicating between threads, showing the {@link #reader} when the
	 * connection has been closed, so it can {@link Thread#join()}.
	 */
	private boolean end = false;
	
	public NetworkProxyConnection(String host, int port) throws IOException {
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

				reader = (new Thread(new SerialReader(inputStream)));
				end = false;
				reader.start();
			}
			contact.connected(id, portName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retvalue;
	}

	@Override
	public boolean disconnect() {
		try {
			socket.close();
			contact.disconnected(id);
			end = true;
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
	public void setConnectionContact(ConnectionContact contact) {
		this.contact = contact;
	}

	public boolean isHandshakeComplete() {
		return handshakeComplete;
	}
	
	/**
	 * A separate class to use as the {@link org.zu.ardulink.connection.serial.SerialConnection#reader}. It is run as a
	 * separate {@link Thread} and manages the incoming data, packaging them
	 * using {@link org.zu.ardulink.connection.serial.SerialConnection#divider} into arrays of <b>int</b>s and
	 * forwarding them using
	 * {@link org.zu.ardulink.connection.ConnectionContact#parseInput(int, int, int[])}.
	 * 
	 */
	private class SerialReader implements Runnable {
		private InputStream in;
		private int[] tempBytes = new int[1024];
		private int numTempBytes = 0;

		public SerialReader(InputStream in) {
			this.in = in;
		}

		public void run() {
			byte[] buffer = new byte[1024];
			int len = -1, i, temp;
			try {
				while (!end) {
					if ((len = this.in.read(buffer)) > -1) {
						for (i = 0; i < len; i++) {
							temp = buffer[i];
							// adjust from C-Byte to Java-Byte
							if (temp < 0)
								temp += 256;
							if (temp == IProtocol.DEFAULT_INCOMING_MESSAGE_DIVIDER) {
								if  (numTempBytes > 0) {
									contact.parseInput(id, numTempBytes,
											tempBytes);
								}
								numTempBytes = 0;
							} else {
								tempBytes[numTempBytes] = temp;
								++numTempBytes;
							}
						}
					}
				}
			} catch (IOException e) {
				end = true;
				try {
					outputStream.close();
					inputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				contact.disconnected(id);
				contact.writeLog(id, "connection has been interrupted");
			}
		}
	}
	
}
