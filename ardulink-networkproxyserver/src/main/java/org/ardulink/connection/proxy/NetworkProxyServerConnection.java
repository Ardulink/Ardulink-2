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

import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamReader;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
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
public class NetworkProxyServerConnection implements Runnable {

	private static final Logger logger = LoggerFactory
			.getLogger(NetworkProxyServerConnection.class);

	private final Protocol proto = ArdulinkProtocol2.instance();

	private final Socket socket;

	private Link link;

	public NetworkProxyServerConnection(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			final OutputStream osRemote = socket.getOutputStream();
			InputStream isRemote = socket.getInputStream();

			Handshaker handshaker = new Handshaker(isRemote, osRemote) {
				@Override
				protected Link newLink(Configurer configurer) throws Exception {
					return Links.getLink(configurer);
				}
			};

			Link link = getRoot(handshaker.doHandshake());
			checkState(link instanceof ConnectionBasedLink,
					"Only %s links supported for now (got %s)",
					ConnectionBasedLink.class.getName(), link.getClass());

			final Connection connection = ((ConnectionBasedLink) link)
					.getConnection();
			connection.addListener(new Connection.ListenerAdapter() {
				@Override
				public void received(byte[] bytes) throws IOException {
					osRemote.write(bytes);
					osRemote.write(proto.getSeparator());
				}
			});

			StreamReader streamReader = new StreamReader(isRemote) {
				@Override
				protected void received(byte[] bytes) throws Exception {
					connection.write(bytes);
					connection.write(proto.getSeparator());
				}
			};
			try {
				streamReader.readUntilClosed(proto.getSeparator());
			} finally {
				streamReader.close();
			}
		} catch (Exception e) {
			logger.error("Error while doing proxy", e);
		} finally {
			logger.info("{} connection closed.",
					socket.getRemoteSocketAddress());
			close(link);
			close(socket);
		}
	}

	private Link getRoot(Link link) {
		while (link instanceof LinkDelegate) {
			link = ((LinkDelegate) link).getDelegate();
		}
		return link;
	}

	private void close(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			logger.error("Error closing socket {}", socket, e);
		}
	}

	private void close(Link link) {
		if (link != null) {
			try {
				link.close();
			} catch (Exception e) {
				logger.error("Error disconnecting link {}", link, e);
			}
		}
	}

}
