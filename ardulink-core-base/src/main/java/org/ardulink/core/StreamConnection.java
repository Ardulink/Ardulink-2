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

import static org.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
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
public class StreamConnection extends AbstractConnection implements ByteStreamProcessorProvider {

	private static final Logger logger = LoggerFactory.getLogger(StreamConnection.class);

	private final StreamReader streamReader;
	private final OutputStream outputStream;
	private final ByteStreamProcessor byteStreamProcessor;

	public StreamConnection(InputStream inputStream, OutputStream outputStream,
			ByteStreamProcessor byteStreamProcessor) {
		this.outputStream = outputStream;
		this.byteStreamProcessor = byteStreamProcessor;
		this.streamReader = new StreamReader(inputStream) {
			@Override
			protected void received(byte[] bytes) throws Exception {
				fireReceived(bytes);
			}
		};
		if (inputStream != null) {
			streamReader.runReaderThread(byteStreamProcessor);
		}
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		logger.debug("Stream write {}", bytes);
		outputStream.write(checkNotNull(bytes, "bytes must not be null"));
		outputStream.flush();
		fireSent(bytes);
	}

	@Override
	public void close() throws IOException {
		this.streamReader.close();
		this.outputStream.close();
	}

	@Override
	public ByteStreamProcessor getByteStreamProcessor() {
		return byteStreamProcessor;
	}

}
