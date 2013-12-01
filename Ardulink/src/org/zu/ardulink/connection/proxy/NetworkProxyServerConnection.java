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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.zu.ardulink.Link;
import org.zu.ardulink.RawDataListener;

public class NetworkProxyServerConnection implements Runnable, NetworkProxyMessages, RawDataListener {

	private static Logger logger = Logger.getLogger(NetworkProxyServerConnection.class.getName());

	private Link link;
	private Socket socket;
	
	private BufferedReader bufferedReader;
	private InputStream inputStream;
	private PrintWriter printWriter;
	private OutputStream outputStream;

	private boolean closed = false;
	private boolean handshakeComplete = false;
	
	
	public NetworkProxyServerConnection(Socket socket) {
		super();
		this.socket = socket;
		this.link = Link.createInstance(socket.toString());
		this.link.addRawDataListener(this);
	}

	@Override
	public void run() {
		try {
			outputStream = socket.getOutputStream();
			printWriter = new PrintWriter(outputStream, true);
			
			inputStream = socket.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			
			String inputLine = bufferedReader.readLine();
			while (!closed && !handshakeComplete) {
				processInput(inputLine);
				if(!closed && !handshakeComplete) {
					inputLine = bufferedReader.readLine();
				}
            }
			int dataReceived = bufferedReader.read();
			while (dataReceived != -1) {
				String data = "" + (char)dataReceived;
				// System.out.print(data);
				writeSerial(data);
				dataReceived = bufferedReader.read();
            }			
		} catch (IOException e) {
		}
		finally {
			logger.info(socket.getRemoteSocketAddress().toString() + " connection closed.");
			closed = true;
			Link.destroyInstance(socket.toString());
			try {
				socket.close();
			} catch (IOException socketClosingExceptionTry) {
			}
			link = null;
			socket = null;
			bufferedReader = null;
			inputStream = null;
			printWriter = null;
			outputStream = null;
			closed = true;
		}
	}

	private void processInput(String inputLine) throws IOException {
		if(inputLine.equals(STOP_SERVER_CMD)) {
			logger.info("Stop request received.");
			NetworkProxyServer.stop();
			closed = true;
		} else if(inputLine.equals(GET_PORT_LIST_CMD)) {
			List<String> portList = getPortList();
			if(portList == null || portList.size() == 0) {
				printWriter.println(NUMBER_OF_PORTS + "0");
			} else {
				printWriter.println(NUMBER_OF_PORTS + portList.size());
				Iterator<String> it = portList.iterator();
				while(it.hasNext()) {
					printWriter.println(it.next());
				}
			}
			printWriter.flush();
		} else if(inputLine.equals(CONNECT_CMD)) {
			String portName = bufferedReader.readLine();
			Integer baudRate = new Integer(bufferedReader.readLine());
			boolean connected = connect(portName, baudRate);
			try { // wait a wile to avoid messages with connection not valid.
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			if(connected) {
				printWriter.println(OK);
				handshakeComplete = true;
			} else {
				printWriter.println(KO);
			}
			printWriter.flush();
		}
	}

	private List<String> getPortList() {
		return link.getPortList();
	}

	private boolean writeSerial(String message) {
		return link.writeSerial(message);
	}

	private boolean connect(String portName, int baudRate) {
		return link.connect(portName, baudRate);
	}

	@Override
	public void parseInput(String id, int numBytes, int[] message) {
		for(int i = 0; i < numBytes; i++) {
			try {
				outputStream.write(message[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			outputStream.write(Link.MESSAGE_DIVIDER);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
