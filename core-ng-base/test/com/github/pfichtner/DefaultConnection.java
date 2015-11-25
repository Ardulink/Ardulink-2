package com.github.pfichtner;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultConnection implements Connection {

	private final InputStream inputStream;
	private final OutputStream outputStream;
	private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	public DefaultConnection(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		outputStream.write(checkNotNull(bytes, "bytes must not be null"));
		outputStream.flush();
		for (Listener listener : listeners) {
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

	private void runReaderThread(final InputStream inputStream) {
		newSingleThreadExecutor().submit(new Runnable() {

			private final Scanner scanner = new Scanner(inputStream)
					.useDelimiter("\n");

			@Override
			public void run() {
				while (true) {
					try {
						byte[] bytes = this.scanner.next().getBytes();
						for (Listener listener : listeners) {
							listener.received(bytes);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

}
