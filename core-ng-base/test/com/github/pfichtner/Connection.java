package com.github.pfichtner;

import java.io.IOException;

/**
 * Represents a Connection to an Arduino (ArduLink) board. This connection must
 * not be physically for example it can also be represented by a network
 * connection.
 * 
 * @author Peter Fichtner
 */
public interface Connection {

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
		};

		void received(byte[] bytes) throws IOException;
	}

	void write(byte[] bytes) throws IOException;

	/**
	 * Sets a Listener. Please not: Only <b>one</b<> Listener is supported. If
	 * multiple Listeners should be registered they have to been multiplexed by
	 * the caller.
	 * 
	 * @param listener
	 *            the Listener to set. Any existing Listener will be replaced
	 *            silently
	 */
	void setListener(Listener listener);

}
