package com.github.pfichtner.core.proxy;

import static com.github.pfichtner.core.proxy.ProxyLinkConfig.ProxyConnectionToRemote.OK;
import static com.github.pfichtner.core.proxy.ProxyLinkConfig.ProxyConnectionToRemote.Command.CONNECT_CMD;
import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;
import com.github.pfichtner.core.proxy.ProxyLinkConfig.ProxyConnectionToRemote;

public class ProxyLinkFactory implements LinkFactory<ProxyLinkConfig> {

	@Override
	public String getName() {
		return "proxy";
	}

	@Override
	public ConnectionBasedLink newLink(ProxyLinkConfig config)
			throws UnknownHostException, IOException {
		ProxyConnectionToRemote remote = config.getRemote();

		remote.send(CONNECT_CMD.getCommand());
		remote.send(checkNotNull(config.getPort(), "port must not be null"));
		remote.send(String.valueOf(config.getSpeed()));
		if (remote.read().equals(OK)) {
			remote.startBackgroundReader();
		}

		Socket socket = remote.getSocket();
		return new ConnectionBasedLink(new StreamConnection(
				socket.getInputStream(), socket.getOutputStream()),
				config.getProto());
	}

	public ProxyLinkConfig newLinkConfig() {
		return new ProxyLinkConfig();
	}

}
