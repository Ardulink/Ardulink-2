package com.github.pfichtner.core.proxy;

import static org.zu.ardulink.util.Preconditions.checkNotNull;

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
			throws Exception {
		Socket socket = createSocket(config);
		return new StreamConnection(socket.getInputStream(),
				socket.getOutputStream());
	}

	private Socket createSocket(ProxyConnectionConfig config)
			throws UnknownHostException, IOException {
		return new Socket(checkNotNull(config.getHost(),
				"host must not be null"), config.getPort());
	}

	public ProxyConnectionConfig newConnectionConfig() {
		return new ProxyConnectionConfig();
	}

}
