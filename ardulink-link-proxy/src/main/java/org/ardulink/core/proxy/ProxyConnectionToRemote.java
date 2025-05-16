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

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.ardulink.core.proxy.ProxyConnectionToRemote.Command.GET_PORT_LIST_CMD;
import static org.ardulink.util.Closeables.closeQuietly;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

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

		CONNECT_CMD("connect"), //
		GET_PORT_LIST_CMD("get_port_list");

		private final String command;

		private Command(String command) {
			this.command = format("ardulink:networkproxyserver:%s", command);
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
	private final Scanner inScanner;
	private final PrintWriter outWriter;

	@SuppressWarnings("resource")
	public ProxyConnectionToRemote(String host, int port) throws IOException {
		this.host = host;
		this.socket = new Socket(host, port);
		this.inScanner = new Scanner(socket.getInputStream()).useDelimiter(quote(PROXY_CONNECTION_SEPARATOR));
		this.outWriter = new PrintWriter(socket.getOutputStream(), false);
	}

	public List<String> getPortList() {
		send(GET_PORT_LIST_CMD);
		int numberOfPorts = numberOfPorts(checkNotNull(read(), "invalid response from %s, got null", host));
		return range(0, numberOfPorts).mapToObj(__ -> read()).collect(toList());
	}

	private static int numberOfPorts(String string) {
		checkState(string.startsWith(NUMBER_OF_PORTS), "invalid response: did not start with %s", NUMBER_OF_PORTS);
		return parseInt(string.substring(NUMBER_OF_PORTS.length()));
	}

	public Socket getSocket() {
		return socket;
	}

	public String read() {
		return inScanner.next();
	}

	public void send(Command command) {
		send(command.getCommand());
	}

	public void send(String message) {
		outWriter.print(message);
		outWriter.print(PROXY_CONNECTION_SEPARATOR);
		outWriter.flush();
	}

	@Override
	public void close() {
		closeQuietly(inScanner);
		closeQuietly(outWriter);
		closeQuietly(socket);
	}

}