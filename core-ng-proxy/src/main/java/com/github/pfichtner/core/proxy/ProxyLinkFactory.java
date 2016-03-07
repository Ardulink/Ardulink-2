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

import static com.github.pfichtner.core.proxy.ProxyConnectionToRemote.Command.CONNECT_CMD;
import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;
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
				"Did not receive ok from remote, got {}", response);
		Socket socket = remote.getSocket();
		Protocol proto = ArdulinkProtocol2.instance();
		return new ConnectionBasedLink(new StreamConnection(
				socket.getInputStream(), socket.getOutputStream(), proto),
				proto) {
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
