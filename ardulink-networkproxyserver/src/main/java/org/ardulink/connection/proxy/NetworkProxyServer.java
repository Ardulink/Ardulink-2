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
package org.ardulink.connection.proxy;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.connection.proxy.NetworkProxyMessages.STOP_SERVER_CMD;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class NetworkProxyServer {

	public static final String NAME = "Ardulink Network Proxy Server";
	private static final Logger logger = LoggerFactory.getLogger(NetworkProxyServer.class);

	private interface Command {
		void execute(int portNumber) throws Exception;
	}

	public static class StartCommand implements Command {

		@Override
		public void execute(int portNumber) throws IOException {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			try {
				serverIsUp(portNumber);
				while (true) {
					new Thread(newConnection(serverSocket)).start();
				}
			} finally {
				logger.info(NAME + " stops");
				serverSocket.close();
			}
		}

		protected void serverIsUp(int portNumber) {
			logger.info(NAME + " listening on port {}", portNumber);
		}

		protected NetworkProxyServerConnection newConnection(ServerSocket serverSocket) throws IOException {
			return new NetworkProxyServerConnection(serverSocket.accept());
		}

	}

	public static class StopCommand implements Command {

		@Override
		public void execute(int portNumber) throws IOException {
			InetAddress localHost = InetAddress.getLocalHost();
			Socket socket = new Socket("127.0.0.1", portNumber);
			socket.setSoTimeout((int) SECONDS.toMillis(5));
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			writer.println(STOP_SERVER_CMD);
			writer.close();
			socket.close();
			System.out.println();
			logger.info(NAME + NAME + " stop requested");
		}

	}

	private static final int DEFAULT_LISTENING_PORT = 4478;

	@Argument(required = true, usage = "command", handler = SubCommandHandler.class)
	@SubCommands({ @SubCommand(name = "start", impl = StartCommand.class),
			@SubCommand(name = "stop", impl = StopCommand.class) })
	private Command command;

	@Option(name = "-p", aliases = "--port", usage = "Local port to bind to")
	private int portNumber = DEFAULT_LISTENING_PORT;

	public static void main(String[] args) throws Exception {
		new NetworkProxyServer().doMain(args);
	}

	private void doMain(String[] args) throws Exception {
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

}
