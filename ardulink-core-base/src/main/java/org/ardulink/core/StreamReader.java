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
import java.util.Arrays;
import java.util.Scanner;

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

	private Thread thread;

	public StreamReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	protected InputStream getInputStream() {
		return inputStream;
	}

	public void runReaderThread(final String delimiter) {
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

//	public void readUntilClosed(String delimiter) {
//		Scanner scanner = new Scanner(inputStream);
//		try {
//			scanner.useDelimiter(delimiter);
//			while (scanner.hasNext()) {
//				try {
//					logger.debug("Waiting for data");
//					byte[] bytes = scanner.next().getBytes();
//					logger.debug("Stream read {}", bytes);
//					received(bytes);
//				} catch (Exception e) {
//					logger.error("Error while retrieving data", e);
//				}
//			}
//		} finally {
//			scanner.close();
//		}
//	}

	public void readUntilClosed(String delimiter) {
		byte[] buffer = new byte[1024];
		int len = -1, i, temp;
		boolean end = false;
		int numTempBytes = 0;
		byte[] tempBytes = new byte[1024];

		try {
			while (!end) {
				// if ((in.available()) > 0) {
					if ((len = this.inputStream.read(buffer)) > -1) {
						for (i = 0; i < len; i++) {
							temp = buffer[i];
							 // adjust from C-Byte to Java-Byte
							if (temp < 0)
								temp += 256;
							if (temp == '\n') {
								if  (numTempBytes > 0) {
									received(Arrays.copyOfRange(tempBytes, 0, numTempBytes));
								}
								numTempBytes = 0;
							} else {
								tempBytes[numTempBytes] = (byte)temp;
								++numTempBytes;
							}
						}
					}
				// }
			}
		} catch (Exception e) {
			end = true;
			try {
				inputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
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
