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

package org.ardulink.core.proxy;

import static org.ardulink.core.proxy.ProxyConnectionToRemote.Command.GET_PORT_LIST_CMD;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ProxyConnectionToRemote implements Closeable {

	private static final String PROXY_CONNECTION_SEPARATOR = "\n";

	public enum Command {

		GET_PORT_LIST_CMD("get_port_list"), CONNECT_CMD("connect");

		private static final String PREFIX = "ardulink:networkproxyserver:";
		private final String command;

		Command(String command) {
			this.command = PREFIX + command;
		}

		public String getCommand() {
			return command;
		}

		@Override
		public String toString() {
			return getCommand();
		}

	}

	private static final String NUMBER_OF_PORTS = "NUMBER_OF_PORTS=";

	private final String host;
	private final Socket socket;
	private final Scanner scanner;
	private final PrintWriter printWriter;


	public ProxyConnectionToRemote(String host, int port) throws IOException {
		this.host = host;
		socket = new Socket(host, port);
		this.scanner = new Scanner(socket.getInputStream()).useDelimiter(Pattern.quote(PROXY_CONNECTION_SEPARATOR));
		this.printWriter = new PrintWriter(socket.getOutputStream(), false);
	}

	public List<String> getPortList() throws IOException {
		send(GET_PORT_LIST_CMD.getCommand());
		String numberOfPorts = checkNotNull(read(), "invalid response from %s, got null", host);
		checkState(numberOfPorts.startsWith(NUMBER_OF_PORTS), "invalid response: did not start with %s",
				NUMBER_OF_PORTS);
		int numOfPorts = Integer.parseInt(numberOfPorts.substring(NUMBER_OF_PORTS.length()));
		List<String> retvalue = new ArrayList<>(numOfPorts);
		for (int i = 0; i < numOfPorts; i++) {
			retvalue.add(read());
		}
		return retvalue;
	}

	public Socket getSocket() {
		return socket;
	}

	public String read() throws IOException {
		return scanner.next();
	}

	public void send(String message) {
		printWriter.print(message);
		printWriter.print(PROXY_CONNECTION_SEPARATOR);
		printWriter.flush();
	}

	@Override
	public void close() throws IOException {
		scanner.close();
		printWriter.close();
		socket.close();
	}

}