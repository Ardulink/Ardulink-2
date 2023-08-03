package org.ardulink.core;

import static java.time.Duration.ofMillis;
import static org.ardulink.util.Throwables.propagate;
import static org.ardulink.util.Throwables.propagateIfInstanceOf;
import static org.awaitility.Awaitility.await;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardulink.core.Connection.Listener;
import org.ardulink.core.Connection.ListenerAdapter;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;
import org.junit.jupiter.api.function.Executable;

public class IOStreamWatchExtension extends TypeBasedParameterResolver<IOStreamWatchExtension>
		implements BeforeEachCallback, AfterEachCallback {

	private final ByteArrayOutputStream os = new ByteArrayOutputStream();
	private PipedOutputStream arduinosOutputStream;
	private Connection connection;
	private ConnectionBasedLink link;
	private final AtomicInteger bytesNotYetRead = new AtomicInteger();

	@Override
	public IOStreamWatchExtension resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return new IOStreamWatchExtension();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		PipedInputStream pis = new PipedInputStream();
		this.arduinosOutputStream = new PipedOutputStream(pis);
		ByteStreamProcessor byteStreamProcessor = new ArdulinkProtocol2().newByteStreamProcessor();
		this.connection = new StreamConnection(pis, os, byteStreamProcessor);
		this.connection.addListener(new ListenerAdapter() {
			@Override
			public void received(byte[] bytes) throws IOException {
				IOStreamWatchExtension.this.bytesNotYetRead.addAndGet(-bytes.length);
			}
		});
		this.link = new ConnectionBasedLink(connection, byteStreamProcessor);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		ConnectionBasedLink lLink = this.link();
		if (lLink != null) {
			lLink.close();
		}
	}

	public ConnectionBasedLink link() {
		return link;
	}

	public <T extends Listener> T withListener(T listener, Executable executable) throws Exception {
		this.connection.addListener(listener);
		try {
			executable.execute();
			return listener;
		} catch (Throwable t) {
			propagateIfInstanceOf(t, Exception.class);
			throw propagate(t);
		} finally {
			this.connection.removeListener(listener);
		}
	}

	public void simulateArduinoSend(String message) throws IOException {
		this.arduinosOutputStream.write(message.getBytes());
		this.arduinosOutputStream.write('\n');
		this.bytesNotYetRead.addAndGet(message.getBytes().length + 1);
		waitUntilRead();
	}

	public String toArduinoWasSent() {
		return this.os.toString();
	}

	public void waitUntilRead() {
		await().forever().pollDelay(ofMillis(10)).until(() -> bytesNotYetRead.get() == 0);
	}

}
