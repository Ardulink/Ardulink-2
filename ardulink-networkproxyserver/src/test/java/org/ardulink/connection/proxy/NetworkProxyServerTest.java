package org.ardulink.connection.proxy;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static org.ardulink.util.ServerSockets.freePort;
import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.ardulink.connection.proxy.NetworkProxyServer.StartCommand;
import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.core.proxy.ProxyLinkConfig;
import org.ardulink.core.proxy.ProxyLinkFactory;
import org.ardulink.util.anno.LapsedWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@Timeout(15)
class NetworkProxyServerTest {

	private final StringBuilder proxySideReceived = new StringBuilder();
	private final Connection proxySideConnection = new Connection() {

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
		}

		@Override
		public void write(byte[] bytes) throws IOException {
			proxySideReceived.append(new String(bytes));
		}

		@Override
		public void addListener(Listener listener) {
			// TODO Auto-generated method stub
		}

		@Override
		public void removeListener(Listener listener) {
			// TODO Auto-generated method stub
		}

	};

	private ConnectionBasedLink clientSideLink;

	@BeforeEach
	void setup() throws InterruptedException, UnknownHostException, IOException {
		int freePort = freePort();
		startServerInBackground(freePort);
		this.clientSideLink = clientLinkToServer("localhost", freePort);
	}

	@Test
	void proxyServerDoesReceiveMessagesSentByClient() throws Exception {
		int times = 3;
		for (int i = 0; i < times; i++) {
			this.clientSideLink.switchAnalogPin(analogPin(1), 2);
		}

		String message = alpProtocolMessage(POWER_PIN_INTENSITY).forPin(1).withValue(2) + "\n";
		assertReceived(message + message + message, times);
	}

	@LapsedWith(value = JDK8, module = "Awaitility")
	private void assertReceived(String expectedMsg, int times) throws Exception {
		while (!proxySideReceived.toString().equals(expectedMsg)) {
			MILLISECONDS.sleep(50);
		}
	}

	private ConnectionBasedLink clientLinkToServer(String hostname, int port) throws UnknownHostException, IOException {
		// TODO PF use Links?
		// Links.getLink(URIs.newURI(String.format("ardulink://proxy?tcphost=%s&tcpport=%s&port=%s",
		// hostname, port, "someNonNullPort")));
		ProxyLinkFactory linkFactory = new ProxyLinkFactory();
		ProxyLinkConfig linkConfig = linkFactory.newLinkConfig();
		return linkFactory.newLink(configure(linkConfig, hostname, port));
	}

	private void startServerInBackground(final int freePort) throws InterruptedException {
		final ReentrantLock lock = new ReentrantLock();
		final Condition waitUntilServerIsUp = lock.newCondition();
		new Thread() {

			@Override
			public void run() {
				new StartCommand() {

					@Override
					protected void serverIsUp(int portNumber) {
						super.serverIsUp(portNumber);
						lock.lock();
						try {
							waitUntilServerIsUp.signal();
						} finally {
							lock.unlock();
						}
					}

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
										ByteStreamProcessor byteStreamProcessor = new ArdulinkProtocol2()
												.newByteStreamProcessor();
										return new ConnectionBasedLink(proxySideConnection, byteStreamProcessor);
									}
								});
								return configurer;
							}
						};
					}
				}.execute(freePort);
			}
		}.start();

		lock.lock();
		try {
			waitUntilServerIsUp.await();
		} finally {
			lock.unlock();
		}
	}

	private ProxyLinkConfig configure(ProxyLinkConfig linkConfig, String hostname, int tcpPort) {
		linkConfig.setTcphost(hostname);
		linkConfig.setTcpport(tcpPort);
		linkConfig.setPort("anything non null");
		return linkConfig;
	}

}
