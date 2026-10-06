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

import static org.ardulink.util.Instance.castTo;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Streams.getLast;
import static org.ardulink.util.Streams.unfold;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Optional;

import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamReader;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.util.Instance;
import org.ardulink.util.Throwables;
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

	private static final Logger logger = LoggerFactory.getLogger(NetworkProxyServerConnection.class);

	private final Socket socket;

	private Link link;

	public NetworkProxyServerConnection(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			OutputStream osRemote = socket.getOutputStream();
			InputStream isRemote = socket.getInputStream();

			Link link = getRoot(handshaker(isRemote, osRemote).doHandshake());
			checkState(link instanceof ConnectionBasedLink, "Only %s links supported for now (got %s)",
					ConnectionBasedLink.class.getName(), link.getClass());

			ConnectionBasedLink cbLink = (ConnectionBasedLink) link;
			cbLink.getConnection().addListener(new Connection.ListenerAdapter() {
				@Override
				public void received(byte[] bytes) {
					try {
						osRemote.write(bytes);
					} catch (IOException e) {
						throw Throwables.propagate(e);
					}
				}
			});

			try (StreamReader streamReader = new StreamReader(isRemote) {
				@Override
				protected void received(byte[] bytes) throws Exception {
					cbLink.getConnection().write(bytes);
				}
			}) {
				streamReader.readUntilClosed();
			}
		} catch (Exception e) {
			logger.error("Error while doing proxy", e);
		} finally {
			logger.info("{} connection closed.", socket.getRemoteSocketAddress());
			close(link);
			close(socket);
		}
	}

	protected Handshaker handshaker(InputStream isRemote, OutputStream osRemote) {
		return new Handshaker(isRemote, osRemote);
	}

	private Link getRoot(Link link) {
		Instance<LinkDelegate> linkDelegate = castTo(LinkDelegate.class);
		return getLast(unfold(link, l -> linkDelegate.map(l, LinkDelegate::getDelegate))).orElse(link);
	}

	private void close(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			logger.error("Error closing socket {}", socket, e);
		}
	}

	private void close(Link link) {
		Optional.ofNullable(link).ifPresent(l -> {
			try {
				l.close();
			} catch (IOException e) {
				logger.error("Error closing link {}", l, e);
			}
		});
	}

}
