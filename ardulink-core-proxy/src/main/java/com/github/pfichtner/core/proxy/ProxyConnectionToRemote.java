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

package com.github.pfichtner.core.proxy;

import static com.github.pfichtner.core.proxy.ProxyConnectionToRemote.Command.GET_PORT_LIST_CMD;
import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol2;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ProxyConnectionToRemote implements Closeable {

	public static enum Command {

		GET_PORT_LIST_CMD("get_port_list"), CONNECT_CMD("connect");

		private static final String PREFIX = "ardulink:networkproxyserver:";
		private final String command;

		private Command(String command) {
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
	private final Protocol proto = ArdulinkProtocol2.instance();
	private final BufferedReader bufferedReader;
	private final PrintWriter printWriter;

	public ProxyConnectionToRemote(String host, int port)
			throws UnknownHostException, IOException {
		this.host = host;
		socket = new Socket(host, port);
		this.bufferedReader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		this.printWriter = new PrintWriter(socket.getOutputStream(), false);
	}

	public List<String> getPortList() throws IOException {
		send(GET_PORT_LIST_CMD.getCommand());
		String numberOfPorts = checkNotNull(read(),
				"invalid response from %s, got null", host);
		checkState(numberOfPorts.startsWith(NUMBER_OF_PORTS),
				"invalid response: did not start with %s", NUMBER_OF_PORTS);
		int numOfPorts = Integer.parseInt(numberOfPorts
				.substring(NUMBER_OF_PORTS.length()));
		List<String> retvalue = new ArrayList<String>(numOfPorts);
		for (int i = 0; i < numOfPorts; i++) {
			retvalue.add(read());
		}
		return retvalue;
	}

	public Socket getSocket() {
		return socket;
	}

	public String read() throws IOException {
		return bufferedReader.readLine();
	}

	public void send(String message) {
		printWriter.print(message);
		printWriter.print(new String(proto.getSeparator()));
		printWriter.flush();
	}

	@Override
	public void close() throws IOException {
		bufferedReader.close();
		printWriter.close();
		socket.close();
	}

}