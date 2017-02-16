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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ardulink.util.ByteArray;

/**
 * Scanner for Stream. It returns byte arrays from a stream splitted
 * by a delimiter
 */
public class StreamScanner {
	
	private InputStream inputStream;
	private byte[] delimiter;
	
	/**
	 * How many bytes read at once from the inputStream
	 */
	private byte[] readBuffer;
	private int readBufferLen = 1;

	/**
	 * Where read bytes are stored
	 */
	private ByteArrayOutputStream bufferOS = new ByteArrayOutputStream(1024);
	
	private ByteArray underBuffer = new ByteArray(new byte[] {});
	
	private boolean interrupted = false;
	
	public StreamScanner(InputStream inputStream, byte[] delimiter) {
		super();
		this.inputStream = inputStream;
		this.delimiter = delimiter;
		readBuffer = new byte[readBufferLen];
	}

	public StreamScanner(InputStream inputStream, byte[] delimiter, int bufferReadLen) {
		this(inputStream, delimiter);
		this.readBufferLen = bufferReadLen;
		readBuffer = new byte[bufferReadLen];
	}

	public boolean hasNext() throws IOException {
		underBuffer.resetWith(bufferOS);
		int bytesRead = 0;
		while(!underBuffer.contains(delimiter) && bytesRead != -1) {
			// Not interested in anymore exit asap
			if(interrupted) {
				return false;
			}

			bytesRead = read();
			if(bytesRead > 0) {
				underBuffer.resetWith(bufferOS);
			}
		}
		
		if(bytesRead == -1) {
			return false;
		}

		return true;
	}

	public byte[] next() throws IOException {
		
		byte[] retvalue = null;
		if(hasNext()) {
			underBuffer.resetWith(bufferOS);
			retvalue = underBuffer.next(delimiter);
			bufferOS.reset();
			bufferOS.write(underBuffer.getRemainingBytes());
		}
		return retvalue;
	}

	public void close() {
		try {
			inputStream.close();
			bufferOS.close();
		} catch(IOException e) {}
	}

	private int read() throws IOException {
		int bytesRead = inputStream.read(readBuffer);
		if(bytesRead > 0) {
			bufferOS.write(readBuffer, 0, bytesRead);
		}
		return bytesRead;
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public void interrupt() {
		this.interrupted = true;
	}
}
