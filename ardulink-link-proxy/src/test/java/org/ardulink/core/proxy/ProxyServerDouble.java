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

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static org.ardulink.core.proxy.ProxyLinkFactory.OK;
import static org.ardulink.util.Closeables.closeQuietly;
import static org.ardulink.util.Throwables.propagate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
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
public class ProxyServerDouble implements BeforeEachCallback, AfterEachCallback {

	private static final Logger logger = LoggerFactory.getLogger(ProxyServerDouble.class);

	public static ServerSocket newSocket(int port) {
		try {
			return new ServerSocket(port);
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private final Thread thread;
	private Map<String, List<String>> answers = makeMap(1);
	private final List<String> received = new ArrayList<>();

	private ServerSocket serverSocket;

	public ProxyServerDouble() {
		this(newSocket(0));
	}

	public ProxyServerDouble(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		this.thread = new Thread() {

			@Override
			public void run() {
				try {
					Socket clientSocket = serverSocket.accept();
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), false);
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

					String line;
					while ((line = in.readLine()) != null) {
						logger.info("Read {}", line);
						received.add(line);
						answers.getOrDefault(line, emptyList()).stream() //
								.peek(m -> logger.info("Responding {}", m)) //
								.map(m -> m + "\n") //
								.forEach(out::print);
						out.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		this.thread.start();
	}

	@Override
	public void afterEach(ExtensionContext context) {
		this.thread.interrupt();
		closeQuietly(this.serverSocket);
	}

	public int getLocalPort() {
		return this.serverSocket.getLocalPort();
	}

	private static String proxyMessage(String message) {
		return format("ardulink:networkproxyserver:%s", message);
	}

	private List<String> portList(int numberOfPorts) {
		return concat(Stream.of("NUMBER_OF_PORTS=" + numberOfPorts), portNames(numberOfPorts)).collect(toList());
	}

	private Stream<String> portNames(int numberOfPorts) {
		return range(0, numberOfPorts).mapToObj(ProxyServerDouble::portName);
	}

	public static String portName(int i) {
		return "myPortNr" + i;
	}

	public ProxyServerDouble setNumberOfPorts(int numberOfPorts) {
		this.answers = makeMap(numberOfPorts);
		return this;
	}

	private Map<String, List<String>> makeMap(int numberOfPorts) {
		return Map.of( //
				proxyMessage("get_port_list"), portList(numberOfPorts), //
				proxyMessage("connect"), singletonList(OK) //
		);
	}

	public List<String> received() {
		return received;
	}

}