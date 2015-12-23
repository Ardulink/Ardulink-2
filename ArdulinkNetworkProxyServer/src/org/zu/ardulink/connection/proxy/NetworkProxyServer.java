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

import static java.lang.Math.max;
import static org.zu.ardulink.connection.proxy.NetworkProxyConnection.DEFAULT_LISTENING_PORT;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;
import org.zu.ardulink.Link;
import org.zu.ardulink.connection.pi.RaspberryPIConnection;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class NetworkProxyServer implements NetworkProxyMessages {

	private interface Command {
		void execute(int portNumber);
	}

	public static class StartCommand implements Command {

		@Override
		public void execute(int portNumber) {
			try {
				ServerSocket serverSocket = new ServerSocket(portNumber);
				try {
					System.out
							.println("Ardulink Network Proxy Server running...");
					while (listening) {
						NetworkProxyServerConnection connection = new NetworkProxyServerConnection(
								serverSocket.accept());
						Thread thread = new Thread(connection);
						thread.start();
						TimeUnit.SECONDS.sleep(2);
					}
				} finally {
					serverSocket.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
			System.out.println("Ardulink Network Proxy Server stops.");
		}

	}

	public static class StopCommand implements Command {

		@Override
		public void execute(int portNumber) {
			try {
				Socket socket = new Socket("127.0.0.1", portNumber);
				PrintWriter writer = new PrintWriter(socket.getOutputStream(),
						true);
				writer.println(STOP_SERVER_CMD);
				writer.close();
				socket.close();
				System.out
						.println("Ardulink Network Proxy Server stop requested.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static boolean listening = true;

	@Argument(required = true, usage = "command", handler = SubCommandHandler.class)
	@SubCommands({ @SubCommand(name = "start", impl = StartCommand.class),
			@SubCommand(name = "stop", impl = StopCommand.class) })
	private Command command;

	@Option(name = "-p", aliases = "--port", usage = "Local port to bind to")
	private int portNumber = DEFAULT_LISTENING_PORT;

	@Option(name = "-rasp", aliases = "--raspberryGPIO", handler=BooleanOptionHandler.class, usage = "Link used is for Raspberry PI GPIO")
	private static boolean raspGPIOConnection;
	
	private static Map<String, Integer> linkUsers = new HashMap<String, Integer>();

	public static void main(String[] args) {
		new NetworkProxyServer().doMain(args);
	}

	private void doMain(String[] args) {
		CmdLineParser cmdLineParser = new CmdLineParser(this);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage(System.err);
			return;
		}
		command.execute(portNumber);
	}

	public static void stop() {
		listening = false;
	}

	public static Link connect(String portName, int baudRate) {
		Link link = Link.getInstance(portName);
		if (link == null) {
			link = retrieveInstance(portName);
		}
		if (!link.isConnected()) {
			link.connect(portName, baudRate);
		}
		addUserToLink(portName);
		return link;
	}

	public static boolean disconnect(String portName) {
		boolean retvalue = false;
		if (!Link.getDefaultInstance().getName().equals(portName)) {
			Link link = Link.getInstance(portName);
			if (link != null) {
				int currentUsers = removeUserFromLink(portName);
				if (currentUsers == 0) {
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
		synchronized (linkUsers) {
			Integer users = linkUsers.get(portName);
			int retvalue = users == null ? 1 : users + 1;
			linkUsers.put(portName, retvalue);
			return retvalue;
		}
	}

	private static int removeUserFromLink(String portName) {
		synchronized (linkUsers) {
			Integer users = linkUsers.get(portName);
			int retvalue = users == null ? 0 : max(0, users - 1);
			linkUsers.put(portName, retvalue);
			return retvalue;
		}
	}

	public static List<String> getPortList() {
		List<String> retvalue = null;
		if(raspGPIOConnection) {
			retvalue = retrieveInstance(RaspberryPIConnection.CONNECTION_NAME).getPortList();
		} else {
			retvalue = Link.getDefaultInstance().getPortList();
		}
		return retvalue;
	}

	private static Link retrieveInstance(String portName) {
		Link retvalue;
		if(raspGPIOConnection) {
			retvalue = Link.createInstance(portName, new RaspberryPIConnection());
		} else {
			retvalue = Link.createInstance(portName);
		}
		return retvalue;
	}
}
