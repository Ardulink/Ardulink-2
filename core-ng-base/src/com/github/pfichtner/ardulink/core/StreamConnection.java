package com.github.pfichtner.ardulink.core;

import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamConnection implements Connection {

	private static final Logger logger = LoggerFactory
			.getLogger(StreamConnection.class);

	private final InputStream inputStream;
	private final OutputStream outputStream;
	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	private Thread thread;

	public StreamConnection(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		this.outputStream.write(checkNotNull(bytes, "bytes must not be null"));
		this.outputStream.flush();
		for (Listener listener : this.listeners) {
			listener.sent(bytes);
		}
	}

	@Override
	public void addListener(Listener listener) {
		this.listeners.add(listener);
		if (this.inputStream != null) {
			runReaderThread(this.inputStream);
		}
	}

	@Override
	public void removeListener(Listener listener) {
		this.listeners.remove(listener);
	}

	private void runReaderThread(final InputStream inputStream) {
		this.thread = new Thread(runnable(inputStream));
		this.thread.setDaemon(true);
		this.thread.start();
	}

	private Runnable runnable(final InputStream inputStream) {
		return new Runnable() {

			private final Scanner scanner = new Scanner(inputStream)
					.useDelimiter("\n");

			@Override
			public void run() {
				while (this.scanner.hasNext()) {
					try {
						logger.debug("Waiting for data");
						byte[] bytes = this.scanner.next().getBytes();
						logger.debug("Received data {}", bytes);
						callListeners(bytes);
					} catch (Exception e) {
						logger.error("Error while retrieving data", e);
					}
				}
			}

			private void callListeners(byte[] bytes) {
				for (Listener listener : StreamConnection.this.listeners) {
					try {
						listener.received(bytes);
					} catch (Exception e) {
						logger.error("Listener {} failure", listener, e);
					}
				}
			}
		};
	}

	@Override
	public void close() throws IOException {
		Thread locThread = this.thread;
		if (locThread != null) {
			locThread.interrupt();
		}
	}

}
