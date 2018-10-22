package org.ardulink.connection.proxy;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static org.ardulink.util.ServerSockets.freePort;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.ardulink.connection.proxy.NetworkProxyServer.StartCommand;
import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.core.proxy.ProxyLinkConfig;
import org.ardulink.core.proxy.ProxyLinkFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class NetworkProxyServerTest {

	@Rule
	public Timeout timeout = new Timeout(15, SECONDS);

	private final Connection mockedConnection = mock(Connection.class);

	@Test
	public void proxyServerDoesReceiveMessagesSentByClient() throws UnknownHostException, IOException {
		int freePort = freePort();
		startServerInBackground(freePort);
		Link link = clientLinkToServer("localhost", freePort);

		int times = 3;
		for (int i = 0; i < times; i++) {
			link.switchAnalogPin(analogPin(1), 2);
		}

		String expectedMsg = alpProtocolMessage(POWER_PIN_INTENSITY).forPin(1).withValue(2);
		verify(mockedConnection, times(times)).write(expectedMsg.getBytes());
	}

	private Link clientLinkToServer(String hostname, int port) throws UnknownHostException, IOException {
		ProxyLinkFactory linkFactory = new ProxyLinkFactory();
		ProxyLinkConfig linkConfig = linkFactory.newLinkConfig();
		return linkFactory.newLink(configure(linkConfig, hostname, port));
	}

	private void startServerInBackground(final int freePort) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				newStartCommand().execute(freePort);
			}

		}).start();
	}

	private StartCommand newStartCommand() {
		return new StartCommand() {
			@Override
			protected NetworkProxyServerConnection newConnection(ServerSocket serverSocket) throws IOException {
				return new NetworkProxyServerConnection(serverSocket.accept()) {
					@Override
					protected Handshaker handshaker(InputStream isRemote, OutputStream osRemote) {
						return new Handshaker(isRemote, osRemote, configurer());
					}

					private Configurer configurer() {
						Configurer configurer = mock(Configurer.class);
						when(configurer.getAttributes()).thenReturn(singletonList("port"));
						when(configurer.getAttribute(anyString())).thenAnswer(new Answer<ConfigAttribute>() {
							@Override
							public ConfigAttribute answer(InvocationOnMock invocation) {
								return configAttributeofName((String) invocation.getArguments()[0]);
							}

							private ConfigAttribute configAttributeofName(String key) {
								ConfigAttribute attribute = mock(ConfigAttribute.class);
								when(attribute.getName()).thenReturn(key);
								return attribute;
							}
						});
						when(configurer.newLink()).then(new Answer<Link>() {
							@Override
							public Link answer(InvocationOnMock invocation) {
								return new ConnectionBasedLink(mockedConnection, ArdulinkProtocol2.instance());
							}
						});
						return configurer;
					}
				};
			}
		};
	}

	private ProxyLinkConfig configure(ProxyLinkConfig linkConfig, String hostname, int tcpPort) {
		linkConfig.setTcphost(hostname);
		linkConfig.setTcpport(tcpPort);
		linkConfig.setPort("anything non null");
		return linkConfig;
	}

}
