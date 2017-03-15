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

import java.io.IOException;
import java.io.InputStream;

import org.ardulink.util.ByteArray;

/**
 * Scanner for Stream. It returns byte arrays from a stream splitted by a
 * delimiter.
 */
public class StreamScanner {

	private final InputStream inputStream;
	private final byte[] delimiter;

	private final byte[] readBuffer;

	private ByteArray underBuffer = new ByteArray(new byte[0]);

	private boolean interrupted;

	public StreamScanner(InputStream inputStream, byte[] delimiter) {
		this(inputStream, delimiter, 1);
	}

	public StreamScanner(InputStream inputStream, byte[] delimiter,
			int bufferReadLen) {
		this.inputStream = inputStream;
		this.delimiter = delimiter;
		this.readBuffer = new byte[bufferReadLen];
	}

	public boolean hasNext() throws IOException {
		int bytesRead = 0;
		while (!underBuffer.contains(delimiter) && (bytesRead = read()) != -1) {
			// Not interested in anymore exit asap
			if (interrupted) {
				return false;
			}
		}
		return bytesRead != -1;
	}

	private int read() throws IOException {
		int bytesRead = inputStream.read(readBuffer);
		if (bytesRead > 0) {
			underBuffer.append(readBuffer, bytesRead);
		}
		return bytesRead;
	}

	public byte[] next() throws IOException {
		return hasNext() ? underBuffer.next(delimiter) : null;
	}

	public void close() {
		try {
			inputStream.close();
		} catch (IOException e) {
			// TODO LZ why is this Exception swallowed?
		}
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public void interrupt() {
		this.interrupted = true;
	}

}
