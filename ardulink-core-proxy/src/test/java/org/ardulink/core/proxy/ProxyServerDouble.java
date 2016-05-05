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

import static org.ardulink.core.proxy.ProxyLinkFactory.OK;
import static org.ardulink.util.Throwables.propagate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ProxyServerDouble extends ExternalResource {
	
	private final Protocol tcpProto = ArdulinkProtocol2.instance();

	private static final Logger logger = LoggerFactory
			.getLogger(ProxyServerDouble.class);

	public static ServerSocket newSocket(int port) {
		try {
			return new ServerSocket(port);
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private final Thread thread;
	private Map<String, List<String>> answers = makeMap(1);
	private final List<String> received = new ArrayList<String>();

	private ServerSocket serverSocket;

	public ProxyServerDouble() {
		this(newSocket(0));
	}

	public ProxyServerDouble(final ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		thread = new Thread() {

			@Override
			public void run() {
				try {
					Socket clientSocket = serverSocket.accept();
					PrintWriter out = new PrintWriter(
							clientSocket.getOutputStream(), false);
					BufferedReader in = new BufferedReader(
							new InputStreamReader(clientSocket.getInputStream()));

					String line;
					while ((line = in.readLine()) != null) {
						logger.info("Read {}", line);
						received.add(line);
						List<String> responses = answers.get(line);
						if (responses != null) {
							for (String response : responses) {
								logger.info("Responding {}", response);
								out.print(response);
								out.print(new String(tcpProto.getSeparator()));
								out.flush();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
		};
	}

	@Override
	protected void after() {
		this.thread.interrupt();
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	@Override
	protected void before() throws Throwable {
		this.thread.start();
	}

	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	private Map<String, List<String>> makeMap(int numberOfPorts) {
		Map<String, List<String>> answers = new HashMap<String, List<String>>();
		answers.put("ardulink:networkproxyserver:get_port_list",
				portList(numberOfPorts));
		answers.put("ardulink:networkproxyserver:connect", Arrays.asList(OK));
		return answers;
	}

	private List<String> portList(int numberOfPorts) {
		List<String> subAnwser = new ArrayList<String>();
		subAnwser.add("NUMBER_OF_PORTS=" + numberOfPorts);
		for (int i = 0; i < numberOfPorts; i++) {
			subAnwser.add("myPortNr" + i);
		}
		return subAnwser;
	}

	public ProxyServerDouble setNumberOfPorts(int numberOfPorts) {
		this.answers = makeMap(numberOfPorts);
		return this;
	}

	public List<String> getReceived() {
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return received;
	}

}