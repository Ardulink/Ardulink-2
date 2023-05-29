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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

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
public abstract class StreamReader implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(StreamReader.class);

	private final InputStream inputStream;

	private Thread thread;

	protected StreamReader(InputStream inputStream) {
		this.inputStream = checkNotNull(inputStream, "InputStream must not be null");
	}

	public void runReaderThread(ByteStreamProcessor byteStreamProcessor) {
		this.thread = new Thread(() -> readUntilClosed(byteStreamProcessor));
		this.thread.setDaemon(true);
		this.thread.start();
	}

	public void runReaderThread() {
		this.thread = new Thread(() -> readUntilClosed());
		this.thread.setDaemon(true);
		this.thread.start();
	}

	public void readUntilClosed() {
		try {
			int read;
			while ((read = inputStream.read()) != -1 && !isInterrupted()) {
				received(read);
			}
		} catch (Exception e) {
			logger.error("Error while Reader Initialization", e);
		}
	}

	private void received(int read) {
		try {
			received(new byte[] { (byte) read });
		} catch (Exception e) {
			logger.error("Error while retrieving data", e);
		}
	}

	public void readUntilClosed(ByteStreamProcessor byteStreamProcessor) {
		try {
			int read;
			while ((read = inputStream.read()) != -1 && !isInterrupted()) {
				try {
					byte b = (byte) read;
					received(new byte[] { b });
					byteStreamProcessor.process(b);
				} catch (Exception e) {
					logger.error("Error while retrieving data", e);
				}
			}
		} catch (Exception e) {
			logger.error("Error while Reader Initialization", e);
		}
	}

	private boolean isInterrupted() {
		return this.thread != null && this.thread.isInterrupted();
	}

	protected abstract void received(byte[] bytes) throws Exception;

	@Override
	public void close() throws IOException {
		Optional.ofNullable(thread).ifPresent(Thread::interrupt);
		inputStream.close();
	}

}