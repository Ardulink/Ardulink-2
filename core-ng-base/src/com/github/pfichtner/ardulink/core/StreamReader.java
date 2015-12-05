package com.github.pfichtner.ardulink.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StreamReader implements Closeable {

	private static final Logger logger = LoggerFactory
			.getLogger(StreamReader.class);

	private final InputStream inputStream;

	private Thread thread;

	public StreamReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	protected InputStream getInputStream() {
		return inputStream;
	}

	protected void runReaderThread(final String delimiter) {
		this.thread = new Thread() {

			private final Scanner scanner = new Scanner(inputStream)
					.useDelimiter(delimiter);

			{
				setDaemon(true);
				start();
			}

			@Override
			public void run() {
				while (this.scanner.hasNext()) {
					try {
						logger.debug("Waiting for data");
						byte[] bytes = this.scanner.next().getBytes();
						logger.debug("Received data {}", bytes);
						received(bytes);
					} catch (Exception e) {
						logger.error("Error while retrieving data", e);
					}
				}
			}

		};
	}

	protected abstract void received(byte[] bytes) throws Exception;

	@Override
	public void close() throws IOException {
		Thread locThread = this.thread;
		if (locThread != null) {
			locThread.interrupt();
		}
	}

}
