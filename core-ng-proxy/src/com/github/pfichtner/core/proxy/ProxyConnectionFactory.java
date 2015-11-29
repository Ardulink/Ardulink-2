package com.github.pfichtner.core.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionFactory;

public class ProxyConnectionFactory implements
		ConnectionFactory<ProxyConnectionConfig> {

	@Override
	public String getName() {
		return "proxy";
	}

	@Override
	public Connection newConnection(ProxyConnectionConfig config)
			throws UnknownHostException, IOException {
		Socket socket = config.getRemote().getSocket();
		return new StreamConnection(socket.getInputStream(),
				socket.getOutputStream());
	}

	public ProxyConnectionConfig newConnectionConfig() {
		return new ProxyConnectionConfig();
	}

}
