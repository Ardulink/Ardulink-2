package com.github.pfichtner.ardulink.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents a Connection to an Arduino (ArduLink) board. This connection must
 * not be physically for example it can also be represented by a network
 * connection.
 * 
 * @author Peter Fichtner
 */
public interface Connection extends Closeable {

	/**
	 * The Listener is called on data received from the Arduino.
	 * 
	 * @author Peter Fichtner
	 */
	interface Listener {
		Listener NULL = new Listener() {

			@Override
			public void received(byte[] bytes) throws IOException {
				// do nothing
			}

			@Override
			public void sent(byte[] bytes) throws IOException {
				// do nothing
			}
		};

		/**
		 * Called whenever a message was received from arduino.
		 * 
		 * @param bytes
		 *            the message read
		 * @throws IOException
		 */
		void received(byte[] bytes) throws IOException;

		/**
		 * Called whenever a message was sent to the arduino.
		 * 
		 * @param bytes
		 *            the message read
		 * @throws IOException
		 */
		void sent(byte[] bytes) throws IOException;
	}

	class ListenerAdapter implements Listener {
		@Override
		public void received(byte[] bytes) throws IOException {
			// do nothing
		}

		@Override
		public void sent(byte[] bytes) throws IOException {
			// do nothing
		}
	}

	void write(byte[] bytes) throws IOException;

	void addListener(Listener listener);

}
