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

import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.CONNECT_CMD;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.GET_PORT_LIST_CMD;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.NUMBER_OF_PORTS;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.OK;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.STOP_SERVER_CMD;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.StreamReader;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocol.FromArduino;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * @author Peter Fichtner
 * 
 *         [adsense]
 */
public class NetworkProxyServerConnection implements Runnable {

	private static final Logger logger = LoggerFactory
			.getLogger(NetworkProxyServerConnection.class);

	private final List<LinkContainer> links;
	private Socket socket;

	private Link link;

	private Protocol protocol = ArdulinkProtocol.instance();

	private boolean handshakeComplete;

	public NetworkProxyServerConnection(List<LinkContainer> links, Socket socket) {
		this.links = links;
		this.socket = socket;
	}

	@Override
	public void run() {
		try {

			OutputStream outputStream = socket.getOutputStream();

			StreamReader streamReader = new StreamReader(
					socket.getInputStream()) {

				private String handshakeCmd;
				private List<String> handshakes = new ArrayList<String>();

				private void write(Object object) throws IOException {
					write((object instanceof String ? ((String) object)
							: String.valueOf(object)).getBytes());
				}

				@Override
				protected void received(byte[] bytes) throws Exception {
					if (handshakeComplete) {
						FromArduino fromArduino = protocol.fromArduino(bytes);
						// TODO must handle start/stop listening
					} else {
						String inputLine = new String(bytes);
						if (STOP_SERVER_CMD.equals(inputLine)) {
							logger.info("Stop request received.");
							NetworkProxyServer.stop();
						} else if (inputLine.equals(GET_PORT_LIST_CMD)) {
							Object[] portList = getPortList();
							if (portList == null) {
								portList = new Object[0];
							}
							write(NUMBER_OF_PORTS + portList.length);
							for (Object port : portList) {
								write(port);
							}
						} else if (CONNECT_CMD.equals(inputLine)) {
							handshakeCmd = inputLine;
						} else if (CONNECT_CMD.equals(handshakeCmd)
								&& handshakes.size() == 2) {
							connect(handshakes.get(0),
									Integer.parseInt(handshakes.get(1)));
							try {
								// wait a wile to avoid messages with connection
								// not valid.
								TimeUnit.SECONDS.sleep(1);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
							write(OK);
							handshakeCmd = null;
							handshakes.clear();
							handshakeComplete = true;
						} else {
							handshakes.add(inputLine);
						}
					}

				}
			};

		} catch (IOException e) {
		} finally {
			logger.info("{} connection closed.",
					socket.getRemoteSocketAddress());
			if (link != null) {
				try {
					disconnect(link);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				socket.close();
			} catch (IOException socketClosingExceptionTry) {
			}
		}
	}

	private Object[] getPortList() throws URISyntaxException, Exception {
		return getSerialLink().getAttribute("port").getChoiceValues();
	}

	private void disconnect(Link link) throws IOException {
		LinkContainer linkContainer = findExisting(link);
		if (linkContainer != null) {
			int decreaseUsageCounter = linkContainer.decreaseUsageCounter();
			if (decreaseUsageCounter == 0) {
				linkContainer.getLink().close();
				links.remove(linkContainer);
			}
		}
	}

	private void connect(String portName, int baudRate) throws Exception {
		Configurer configurer = getSerialLink();
		configurer.getAttribute("port").setValue(portName);
		configurer.getAttribute("speed").setValue(baudRate);

		CacheKey cacheKey = new CacheKey(configurer);
		LinkContainer container = findExisting(cacheKey);
		if (container == null) {
			links.add(new LinkContainer(configurer.newLink(), cacheKey));
		} else {
			container.increaseUsageCounter();
		}
	}

	public Configurer getSerialLink() throws URISyntaxException {
		Configurer configurer = LinkManager.getInstance().getConfigurer(
				new URI("ardulink://serial"));
		return configurer;
	}

	private LinkContainer findExisting(CacheKey ck) {
		for (LinkContainer linkContainer : links) {
			if (linkContainer.getCacheKey().equals(ck)) {
				return linkContainer;
			}
		}
		return null;
	}

	private LinkContainer findExisting(Link link) {
		for (LinkContainer linkContainer : links) {
			if (linkContainer.getCacheKey().equals(link)) {
				return linkContainer;
			}
		}
		return null;
	}

}
