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

import static org.ardulink.core.proxy.ProxyConnectionToRemote.Command.CONNECT_CMD;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ProxyLinkFactory implements LinkFactory<ProxyLinkConfig> {

	public static final String OK = "OK";

	@Override
	public String getName() {
		return "proxy";
	}

	@Override
	public ConnectionBasedLink newLink(ProxyLinkConfig config)
			throws UnknownHostException, IOException {
		final ProxyConnectionToRemote remote = config.getRemote();

		remote.send(CONNECT_CMD.getCommand());
		remote.send(checkNotNull(config.getPort(), "port must not be null"));
		remote.send(String.valueOf(config.getSpeed()));
		String response = remote.read();
		checkState(OK.equals(response),
				"Did not receive %s from remote, got %s", OK, response);
		Socket socket = remote.getSocket();
		ByteStreamProcessor byteStreamProcessor = new ArdulinkProtocol2().newByteStreamProcessor();
		return new ConnectionBasedLink(
				new StreamConnection(socket.getInputStream(), socket.getOutputStream(), byteStreamProcessor),
				byteStreamProcessor) {
			@Override
			public void close() throws IOException {
				super.close();
				remote.close();
			}
		};
	}

	@Override
	public ProxyLinkConfig newLinkConfig() {
		return new ProxyLinkConfig();
	}

}
