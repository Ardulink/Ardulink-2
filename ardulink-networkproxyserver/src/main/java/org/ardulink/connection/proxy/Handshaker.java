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

import static org.ardulink.connection.proxy.NetworkProxyMessages.CONNECT_CMD;
import static org.ardulink.connection.proxy.NetworkProxyMessages.GET_PORT_LIST_CMD;
import static org.ardulink.connection.proxy.NetworkProxyMessages.KO;
import static org.ardulink.connection.proxy.NetworkProxyMessages.NUMBER_OF_PORTS;
import static org.ardulink.connection.proxy.NetworkProxyMessages.OK;
import static org.ardulink.connection.proxy.NetworkProxyMessages.STOP_SERVER_CMD;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.linkmanager.LinkManager.Configurer;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class Handshaker {

	public static final String PROXY_CONNECTION_SEPARATOR = "\n";

	private final Scanner scanner;
	private final PrintWriter printWriter;
	private final Configurer configurer;

	public Handshaker(InputStream inputStream, OutputStream outputStream) {
		this.configurer = Links.getDefaultConfigurer();
		this.printWriter = new PrintWriter(outputStream);
		this.scanner = new Scanner(inputStream).useDelimiter(Pattern.quote(PROXY_CONNECTION_SEPARATOR));
	}

	/**
	 * Does the handshaking. If the client sends a
	 * {@link NetworkProxyMessages#STOP_SERVER_CMD} command the parent Thread
	 * will be interrupted.
	 * 
	 * @return a Link to communicate with, e.g. a serial one
	 * @throws Exception
	 */
	public Link doHandshake() throws Exception {
		while (scanner.hasNext()) {
			String input = read();
			if (input.equals(STOP_SERVER_CMD)) {
				Thread currentThread = Thread.currentThread();
				currentThread.getThreadGroup().getParent().interrupt();
				currentThread.interrupt();
			} else if (GET_PORT_LIST_CMD.equals(input)) {
				handleGetPortList();
			} else if (CONNECT_CMD.equals(input)) {
				try {
					// Ardulink-1 only did support Proxy to connect to serial links. So the
					// handshake contains serial specific attributes. We should deprecate
					// the Proxy since MQTT supports the same (or even more) features.
					// Otherwise: Proxy should not contain serial specific things but send
					// the ardulink:// URI to connect to (e.g.
					// ardulink://serial?baudrate=9600)
					configurer.getAttribute("port").setValue(read());
					configurer.getAttribute("baudrate").setValue(Integer.valueOf(read()));
					Link link = newLink(configurer);
					write(OK);
					return link;
				} catch (Exception e) {
					write(KO);
				}
			}
			printWriter.flush();
		}
		throw new IllegalStateException("No more data but no " + CONNECT_CMD + " received");
	}

	private void handleGetPortList() throws IOException {
		Object[] portList = getPortList();
		if (portList == null) {
			portList = new Object[0];
		}
		write(NUMBER_OF_PORTS + portList.length);
		for (Object port : portList) {
			write(port);
		}
	}

	private String read() {
		return scanner.next();
	}

	private void write(Object object) throws IOException {
		String message = object instanceof String ? ((String) object) : String.valueOf(object);
		printWriter.write(message);
		printWriter.write(PROXY_CONNECTION_SEPARATOR);
		printWriter.flush();
	}

	protected abstract Link newLink(Configurer configurer) throws Exception;

	private Object[] getPortList() {
		return configurer.getAttribute("port").getChoiceValues();
	}

}
