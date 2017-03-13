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
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class StreamReader implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(StreamReader.class);

	private final InputStream inputStream;
	private StreamScanner scanner;

	private Thread thread;

	public StreamReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void runReaderThread(final byte[] delimiter) {
		scanner = new StreamScanner(inputStream, delimiter);
		this.thread = new Thread() {

			{
				setDaemon(true);
				start();
			}

			@Override
			public void run() {
				readUntilClosed(delimiter);
			}

		};
	}

	public void readUntilClosed(byte[] delimiter) {
		
		try {
						
			while (scanner.hasNext() && !this.thread.isInterrupted()) {
				try {
					logger.debug("Waiting for data");
					byte[] bytes = scanner.next();
					logger.debug("Stream read {}", bytes);
					if(bytes != null) {
						received(bytes);
					}
				} catch (Exception e) {
					logger.error("Error while retrieving data", e);
				}
			}
		} catch (Exception e) {
			logger.error("Error while Reader Initialization", e);
		} finally {
			scanner.close();
		}
	}

	protected abstract void received(byte[] bytes) throws Exception;

	@Override
	public void close() throws IOException {
		Thread locThread = this.thread;
		if (locThread != null) {
			scanner.interrupt();
			locThread.interrupt();
		}
	}

}