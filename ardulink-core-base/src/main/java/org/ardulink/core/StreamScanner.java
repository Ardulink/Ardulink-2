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

	private ByteArrayOutputStream bufferOS = new ByteArrayOutputStream(1024);
	
	public StreamScanner(InputStream inputStream, byte[] delimiter) {
		super();
		this.inputStream = inputStream;
		this.delimiter = delimiter;
	}

	public boolean hasNext() throws IOException {
		ByteArray underBuffer = new ByteArray(bufferOS);
		int bytesRead = 0;
		while(!underBuffer.contains(delimiter) && bytesRead != -1) {
			bytesRead = read();
			if(bytesRead > 0) {
				underBuffer = new ByteArray(bufferOS);
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
			ByteArray underBuffer = new ByteArray(bufferOS);
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
		byte[] buffer = new byte[1024];
		int bytesRead = inputStream.read(buffer);
		if(bytesRead > 0) {
			bufferOS.write(buffer, 0, bytesRead);
		}
		return bytesRead;
	}
	
}
