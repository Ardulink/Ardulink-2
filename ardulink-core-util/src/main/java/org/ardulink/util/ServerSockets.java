package org.ardulink.util;

import java.io.IOException;
import java.net.ServerSocket;

public final class ServerSockets {

	private ServerSockets() {
		super();
	}

	public static int freePort() {
		try {
			ServerSocket socket = new ServerSocket(0);
			socket.close();
			return socket.getLocalPort();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
}
