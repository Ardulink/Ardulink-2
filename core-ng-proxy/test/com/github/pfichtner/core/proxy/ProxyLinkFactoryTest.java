package com.github.pfichtner.core.proxy;

import static com.github.pfichtner.core.proxy.ProxyLinkConfig.ProxyConnectionToRemote.OK;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public class ProxyLinkFactoryTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Test
	public void canConnectWhileConfiguring() throws Exception {
		ServerSocket serverSocket = new ServerSocket(0);
		startServer(serverSocket, makeMap(0));
		int tcpport = serverSocket.getLocalPort();

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport=" + tcpport));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues(), is(new Object[0]));
	}

	private Map<String, List<String>> makeMap(int numberOfPorts) {
		Map<String, List<String>> answers = new HashMap<String, List<String>>();
		answers.put("ardulink:networkproxyserver:get_port_list",
				portList(numberOfPorts));
		answers.put("ardulink:networkproxyserver:connect", Arrays.asList(OK));
		return answers;
	}

	private List<String> portList(int numberOfPorts) {
		List<String> subAnwser = new ArrayList<String>();
		subAnwser.add("NUMBER_OF_PORTS=" + numberOfPorts);
		for (int i = 0; i < numberOfPorts; i++) {
			subAnwser.add("myPortNr" + i);
		}
		return subAnwser;
	}

	@Test
	public void canReadAvailablePorts() throws Exception {
		ServerSocket serverSocket = new ServerSocket(0);
		startServer(serverSocket, makeMap(1));
		int tcpport = serverSocket.getLocalPort();

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport=" + tcpport));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues(),
				is((Object[]) new String[] { "myPortNr0" }));
	}

	@Test
	public void canConnect() throws Exception {
		ServerSocket serverSocket = new ServerSocket(0);
		startServer(serverSocket, makeMap(1));
		int tcpport = serverSocket.getLocalPort();

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport=" + tcpport));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues(),
				is((Object[]) new String[] { "myPortNr0" }));

		Link newLink = configurer.newLink();
		assertThat(newLink, notNullValue());
	}

	private void startServer(final ServerSocket serverSocket,
			final Map<String, List<String>> answers) throws IOException {
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
					List<String> responses = answers.get(line);
					if (responses != null) {
						for (String response : responses) {
							out.println(response);
						}
					}
					out.flush();
				}
			};
		}.start();
	}
}
