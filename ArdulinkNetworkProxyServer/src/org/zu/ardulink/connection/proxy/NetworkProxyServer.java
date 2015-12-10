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

import static org.zu.ardulink.connection.proxy.NetworkProxyConnection.DEFAULT_LISTENING_PORT;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.STOP_SERVER_CMD;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 *         [adsense]
 */
public class NetworkProxyServer {

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
						new Thread(new NetworkProxyServerConnection(
								serverSocket.accept())).start();
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

}
