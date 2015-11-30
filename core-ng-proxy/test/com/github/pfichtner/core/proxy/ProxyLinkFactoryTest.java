package com.github.pfichtner.core.proxy;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Test;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public class ProxyLinkFactoryTest {

	private static final int TIMEOUT = 5000;

	@Test(timeout = TIMEOUT)
	public void canConnectWhileConfiguring() throws Exception {
		ServerSocket serverSocket = new ServerSocket(0);
		startServer(serverSocket, 0);
		int tcpport = serverSocket.getLocalPort();

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport=" + tcpport));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getPossibleValues(), is(new Object[0]));
	}

	@Test(timeout = TIMEOUT)
	public void canReadAvailablePorts() throws Exception {
		ServerSocket serverSocket = new ServerSocket(0);
		startServer(serverSocket, 1);
		int tcpport = serverSocket.getLocalPort();

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport=" + tcpport));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getPossibleValues(),
				is((Object[]) new String[] { "myPortNr0" }));
	}

	@Test(timeout = TIMEOUT)
	public void canConnect() throws Exception {
		ServerSocket serverSocket = new ServerSocket(0);
		startServer(serverSocket, 1);
		int tcpport = serverSocket.getLocalPort();

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport=" + tcpport));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getPossibleValues(),
				is((Object[]) new String[] { "myPortNr0" }));

		Link newLink = configurer.newLink();
		assertThat(newLink, notNullValue());
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
						out.println("NUMBER_OF_PORTS=" + numberOfPorts);
						for (int i = 0; i < numberOfPorts; i++) {
							out.println("myPortNr" + i);
						}
					}
					if ("ardulink:networkproxyserver:connect".equals(line)) {
						out.println("OK");
					}

					out.flush();
				}
			};
		}.start();
	}
}
