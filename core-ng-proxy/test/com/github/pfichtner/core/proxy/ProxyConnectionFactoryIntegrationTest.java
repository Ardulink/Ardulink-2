package com.github.pfichtner.core.proxy;

import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

import org.hamcrest.core.Is;
import org.junit.Test;

import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionManager;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionManager.Configurer;

public class ProxyConnectionFactoryIntegrationTest {

	private static final int TIMEOUT = 5000;

	@Test(timeout = TIMEOUT)
	public void canConnectWhileConfiguring() throws Exception {
		ServerSocket serverSocket = new ServerSocket(0);
		startServer(serverSocket, 0);
		int port = serverSocket.getLocalPort();

		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?host=localhost&port=" + port));
		ConfigAttribute portList = configurer.getAttribute("portlist");
		assertThat(portList.getPossibleValues(), Is.is(new Object[0]));
	}

	private void startServer(final ServerSocket serverSocket,
			final int numberOfPorts) throws IOException {
		new Thread() {
			public void run() {
				try {
					doBg(serverSocket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			private void doBg(final ServerSocket serverSocket)
					throws IOException {
				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(
						clientSocket.getOutputStream(), false);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));

				String line;
				while ((line = in.readLine()) != null) {
					if ("ardulink:networkproxyserver:get_port_list"
							.equals(line)) {
						out.write("NUMBER_OF_PORTS=" + numberOfPorts + "\n");
						out.flush();
					}
				}
			};
		}.start();
	}
}
