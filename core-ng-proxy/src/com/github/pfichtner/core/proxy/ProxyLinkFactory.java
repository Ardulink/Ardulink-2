package com.github.pfichtner.core.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;

public class ProxyLinkFactory implements
		LinkFactory<ProxyLinkConfig> {

	@Override
	public String getName() {
		return "proxy";
	}

	@Override
	public Link newLink(ProxyLinkConfig config)
			throws UnknownHostException, IOException {
		Socket socket = config.getRemote().getSocket();
		return new ConnectionBasedLink(new StreamConnection(
				socket.getInputStream(), socket.getOutputStream()),
				config.getProto());
	}

	public ProxyLinkConfig newLinkConfig() {
		return new ProxyLinkConfig();
	}

}
