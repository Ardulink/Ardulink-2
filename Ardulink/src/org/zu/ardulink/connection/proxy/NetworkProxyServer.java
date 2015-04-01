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

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.zu.ardulink.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class NetworkProxyServer implements NetworkProxyMessages {
	
    private static boolean listening = true;
    public static final int DEFAULT_LISTENING_PORT = 4478;
    
    private static Map<String, Integer> linkUsers = new HashMap<String, Integer>();
        
	public static void main(String[] args) {
	    if (!validateArgs(args)) {
	    	System.out.println("Ardulink Network Proxy Server");
	    	System.out.println("Usage: networkproxyserver command listening_port_number");
	    	System.out.println("command=start|stop");
	        System.exit(1);
	    }

	    String command = args[0];
	    int portNumber = DEFAULT_LISTENING_PORT;
	    if(args.length > 1) {
	    	portNumber = Integer.parseInt(args[1]);
	    }
	    
	    if("stop".equals(command)) {
	    	requestStop(portNumber);
	    } else {
	        
		    try {
		    	ServerSocket serverSocket = new ServerSocket(portNumber);
		    	System.out.println("Ardulink Network Proxy Server running...");
		    	while (listening) {
		    		NetworkProxyServerConnection connection = new NetworkProxyServerConnection(serverSocket.accept());
		    		Thread thread = new Thread(connection);
		    		thread.start();
		    		Thread.sleep(2000);
		    	}
		    } catch (Exception e) {
		    	e.printStackTrace();
		    	System.exit(-1);
		    }
		    System.out.println("Ardulink Network Proxy Server stops.");
	    }
        System.exit(0);
	}

	private static void requestStop(int portNumber) {
		try {
			Socket socket = new Socket("127.0.0.1", portNumber);
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			writer.println(STOP_SERVER_CMD);
			writer.close();
			socket.close();
			System.out.println("Ardulink Network Proxy Server stop requested.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean validateArgs(String[] args) {
		boolean retvalue = true;
		
		if(args == null) { //should never happens
			retvalue = false;
		} else if(args.length < 1) {
			retvalue = false;
		} else if(args.length > 2) {
			retvalue = false;
		} else if(!(args[0].equals("start") || args[0].equals("stop"))) {
	        System.err.println(args[0] + "is not \'start\' or \'stop\'");
			retvalue = false;
		} else if(args.length == 2) { // check if the sencond param is a number
			try {
				Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e) {
				System.out.println(args[1] + "is not a number.");
		        retvalue = false;
			}
		}
		
		return retvalue;
	}

	public static void stop() {
		listening = false;
	}

	public static Link connect(String portName, int baudRate) {
		
		Link link = Link.getInstance(portName);
		if(link == null) {
			link = Link.createInstance(portName);
		}
		if(!link.isConnected()) {
			link.connect(portName, baudRate);
		}
		addUserToLink(portName);
		return link;
	}

	public static boolean disconnect(String portName) {
		boolean retvalue = false;
		if(!Link.getDefaultInstance().getName().equals(portName)) {
			Link link = Link.getInstance(portName);
			if(link != null) {
				int currentUsers = removeUserFromLink(portName);
				if(currentUsers == 0) {
					retvalue = link.disconnect();
					Link.destroyInstance(portName);
				}
			} else {
				removeUserFromLink(portName);
			}
		}
		return retvalue;
	}

	private static int addUserToLink(String portName) {
		int retvalue = 0;
		synchronized (linkUsers) {
			Integer users = linkUsers.get(portName);
			if(users == null) {
				retvalue = 1;
			} else {
				retvalue = users + 1;
			}
			linkUsers.put(portName, retvalue);
		}
		return retvalue;
	}

	private static int removeUserFromLink(String portName) {
		int retvalue = 0;
		synchronized (linkUsers) {
			Integer users = linkUsers.get(portName);
			if(users == null) {
				retvalue = 0;
			} else {
				retvalue = users - 1;
				if(retvalue < 0) {
					retvalue = 0;
				}
			}
			linkUsers.put(portName, retvalue);
		}
		return retvalue;
	}

	
}
