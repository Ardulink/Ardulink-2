package com.github.pfichtner;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class DefaultConnection implements Connection {

	private final InputStream inputStream;
	private final OutputStream outputStream;
	private Listener listener = Listener.NULL;

	public DefaultConnection(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		outputStream.write(checkNotNull(bytes, "bytes must not be null"));
		outputStream.flush();
	}

	@Override
	public void setListener(Listener listener) {
		this.listener = listener;
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
				try {
					DefaultConnection.this.listener.received(this.scanner
							.next().getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

}
