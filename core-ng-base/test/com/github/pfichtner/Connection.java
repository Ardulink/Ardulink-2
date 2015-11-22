package com.github.pfichtner;

import java.io.IOException;

public interface Connection {

	interface Listener {
		Listener NULL = new Listener() {

			@Override
			public void received(byte[] bytes) throws IOException {
				// do nothing
			}
		};

		void received(byte[] bytes) throws IOException;
	}

	void write(byte[] bytes) throws IOException;

	void setListener(Listener listener);

}
