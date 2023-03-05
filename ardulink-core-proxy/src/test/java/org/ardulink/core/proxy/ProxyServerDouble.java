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
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.ardulink.util.Lists;
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
public class ProxyServerDouble implements BeforeEachCallback, AfterEachCallback{

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
	private final List<String> received = Lists.newArrayList();

	private ServerSocket serverSocket;

	public ProxyServerDouble() {
		this(newSocket(0));
	}

	public ProxyServerDouble(ServerSocket serverSocket) {
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
								out.print("\n");
								out.flush();
							}
						}
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
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw propagate(e);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		ProxyServerDouble serverDouble = new ProxyServerDouble(newSocket(4478));
		serverDouble.beforeEach(null);
		serverDouble.thread.join();
	}

	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	private Map<String, List<String>> makeMap(int numberOfPorts) {
		Map<String, List<String>> answers = new HashMap<>();
		answers.put("ardulink:networkproxyserver:get_port_list",
				portList(numberOfPorts));
		answers.put("ardulink:networkproxyserver:connect", Collections.singletonList(OK));
		return answers;
	}

	private List<String> portList(int numberOfPorts) {
		List<String> subAnwser = new ArrayList<>();
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