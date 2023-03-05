package org.ardulink.connection.proxy;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static org.ardulink.util.ServerSockets.freePort;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.ardulink.connection.proxy.NetworkProxyServer.StartCommand;
import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.core.proxy.ProxyLinkConfig;
import org.ardulink.core.proxy.ProxyLinkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(NetworkProxyServerTest.TIMEOUT)
class NetworkProxyServerTest {

	static final int TIMEOUT = 5;

	private final StringBuilder proxySideReceived = new StringBuilder();
	private final Connection proxySideConnection = new Connection() {
	
		@Override
		public void write(byte[] bytes) throws IOException {
			proxySideReceived.append(new String(bytes));
		}
	
		@Override
		public void addListener(Listener listener) {
			// noop
		}
	
		@Override
		public void removeListener(Listener listener) {
			// noop
		}
	
		@Override
		public void close() throws IOException {
			// noop
		}
	
	};

	private ConnectionBasedLink clientSideLink;

	@BeforeEach
	void setup() throws InterruptedException, UnknownHostException, IOException {
		int serverPort = freePort();
		startServerInBackground(serverPort);
		this.clientSideLink = clientLinkToServer("localhost", serverPort);
	}

	@Test
	void proxyServerDoesReceiveMessagesSentByClient() throws Exception {
		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			this.clientSideLink.switchAnalogPin(analogPin(1), 2);
			expected.append(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(1).withValue(2) + "\n");
		}
		assertReceived(expected);
	}

	private void assertReceived(StringBuilder expected) {
		assertReceived(expected.toString());
	}

	private void assertReceived(String expectedMsg) {
		await().timeout(ofSeconds(TIMEOUT * 2)).pollInterval(ofMillis(50)).until(() -> proxySideReceived.toString(),
				expectedMsg::equals);
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
								return new Configurer() {

									@Override
									public Object uniqueIdentifier() {
										return "";
									}

									@Override
									public Collection<String> getAttributes() {
										return singletonList("port");
									}

									@Override
									public ConfigAttribute getAttribute(String key) {
										return configAttributeOfName(key);
									}

									@Override
									public Link newLink() {
										return new ConnectionBasedLink(proxySideConnection,
												new ArdulinkProtocol2().newByteStreamProcessor());
									}

								};
							}

							private ConfigAttribute configAttributeOfName(String key) {
								ConfigAttribute attribute = mock(ConfigAttribute.class);
								when(attribute.getName()).thenReturn(key);
								return attribute;
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
		linkConfig.setPort("anything non-null");
		return linkConfig;
	}

}
