package org.ardulink.util;

import static org.ardulink.util.Throwables.propagate;

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
			throw propagate(e);
		}
	}
}
