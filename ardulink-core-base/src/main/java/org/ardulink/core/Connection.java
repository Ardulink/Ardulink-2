/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ardulink.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * [ardulinktitle] [ardulinkversion]
 * Represents a Connection to an Arduino (ArduLink) board. This connection must
 * not be physically for example it can also be represented by a network
 * connection.
 * 
 * [adsense] 
 */
public interface Connection extends Closeable {

	/**
	 * The Listener is called on data received from the Arduino.
	 * 
	 *  
	 */
	public interface Listener {
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

	void removeListener(Listener listener);

}
