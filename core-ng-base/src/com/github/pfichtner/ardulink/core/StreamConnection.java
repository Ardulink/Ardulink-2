package com.github.pfichtner.ardulink.core;

import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamConnection implements Connection {

	private static final Logger logger = LoggerFactory
			.getLogger(StreamConnection.class);

	private final StreamReader streamReader;

	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	public StreamConnection(InputStream inputStream, OutputStream outputStream) {
		this.streamReader = new StreamReader(inputStream, outputStream) {
			@Override
			protected void received(byte[] bytes) throws Exception {
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
	public void write(byte[] bytes) throws IOException {
		OutputStream os = streamReader.getOutputStream();
		os.write(checkNotNull(bytes, "bytes must not be null"));
		os.flush();
		for (Listener listener : this.listeners) {
			try {
				listener.sent(bytes);
			} catch (Exception e) {
				logger.error("Listener {} failure", listener, e);
			}
		}
	}

	@Override
	public void addListener(Listener listener) {
		this.listeners.add(listener);
		if (streamReader.getInputStream() != null) {
			streamReader.runReaderThread();
		}
	}

	@Override
	public void removeListener(Listener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void close() throws IOException {
		this.streamReader.close();
	}

}
