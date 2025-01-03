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
package org.ardulink.testsupport.junit5;

import static java.time.Duration.ofMillis;
import static java.util.stream.IntStream.rangeClosed;
import static org.ardulink.core.proto.api.Protocols.protoByName;
import static org.ardulink.util.Throwables.propagate;
import static org.ardulink.util.Throwables.propagateIfInstanceOf;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ardulink.core.Connection.Listener;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.util.ByteArray;
import org.ardulink.util.Closeables;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.function.Executable;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 * JUnit5 Extension that provides a stub for a real Arduino.<br>
 * The stub can be configured, on what requests it should answer with what
 * response.<br>
 * 
 * The link to it can be retrieved via {@link #link}.
 * 
 * @see #onReceive(String)
 * @see #onReceive(Pattern)
 * 
 */
public class ArduinoStubExt implements BeforeEachCallback, AfterEachCallback {

	private static final String NO_RESPONSE = null;

	interface ResponseGenerator {

		boolean matches(String received);

		String getResponse();

	}

	public class Adder implements ResponseGenerator {

		private String whenReceived;
		private String thenRespond;

		public Adder(String whenReceived) {
			this.whenReceived = whenReceived;
		}

		public void thenRespond(String thenRespond) {
			this.thenRespond = thenRespond;
			data.add(this);
		}

		@Override
		public boolean matches(String received) {
			return whenReceived.equals(received);
		}

		@Override
		public String getResponse() {
			return thenRespond;
		}

		public void thenDoNotRespond() {
			// do nothing
		}

	}

	private Matcher matcher;

	public class RegexAdder implements ResponseGenerator {

		private Pattern whenReceived;
		private String thenRespond;

		public RegexAdder(Pattern whenReceived) {
			this.whenReceived = whenReceived;
		}

		public void respondWith(String thenRespond) {
			this.thenRespond = thenRespond;
			data.add(this);
		}

		public void doNotRespond() {
			respondWith(NO_RESPONSE);
		}

		@Override
		public boolean matches(String received) {
			Matcher tmpMatcher = whenReceived.matcher(received);
			if (tmpMatcher.matches()) {
				matcher = tmpMatcher;
				return true;
			}
			return false;
		}

		@Override
		public String getResponse() {
			return thenRespond == null ? null : MessageFormat.format(thenRespond, groupValues());
		}

		private Object[] groupValues() {
			return rangeClosed(1, matcher.groupCount()).mapToObj(matcher::group).toArray(Object[]::new);
		}

	}

	private final Protocol protocol;

	private final List<ResponseGenerator> data = new ArrayList<>();
	private final ByteArrayOutputStream os = new ByteArrayOutputStream();
	private OutputStream simulatedArduinoOs;
	private StreamConnection connection;
	private ConnectionBasedLink link;
	private final AtomicInteger bytesNotYetRead = new AtomicInteger();

	public ArduinoStubExt() {
		this(protoByName("ardulink2"));
	}

	public ArduinoStubExt(Protocol protocol) {
		this.protocol = protocol;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		PipedInputStream is = new PipedInputStream();
		this.simulatedArduinoOs = new PipedOutputStream(is);
		ByteStreamProcessor byteStreamProcessor = protocol.newByteStreamProcessor();
		this.connection = new StreamConnection(is, os, byteStreamProcessor);
		this.connection.addListener(new Listener() {

			private final ByteArray bytes = new ByteArray();

			@Override
			public void received(byte[] bytes) throws IOException {
				ArduinoStubExt.this.bytesNotYetRead.addAndGet(-bytes.length);
			}

			@Override
			public void sent(byte[] bytes) throws IOException {
				this.bytes.append(bytes);
				for (ResponseGenerator generator : data) {
					if (generator.matches(new String(this.bytes.copy()))) {
						String response = generator.getResponse();
						if (response != NO_RESPONSE) {
							simulateArduinoSends(response);
							simulatedArduinoOs.flush();
						}
						this.bytes.clear();
					}
				}
			}
		});

		this.link = new ConnectionBasedLink(this.connection);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		Optional.ofNullable(this.link).ifPresent(Closeables::closeQuietly);
	}

	public ConnectionBasedLink link() {
		return link;
	}

	public <T extends Listener> T withListener(T listener, Executable executable) throws Exception {
		this.connection.addListener(listener);
		try {
			executable.execute();
			return listener;
		} catch (Throwable throwable) {
			propagateIfInstanceOf(throwable, Exception.class);
			throw propagate(throwable);
		} finally {
			this.connection.removeListener(listener);
		}
	}

	public void simulateArduinoSends(String... messages) throws IOException {
		for (String message : messages) {
			simulateArduinoSends(message);
		}
	}

	public void simulateArduinoSends(String message) throws IOException {
		byte[] bytes = message.getBytes();
		this.simulatedArduinoOs.write(bytes);
		this.bytesNotYetRead.addAndGet(bytes.length);
		waitUntilRead();
	}

	public String toArduinoWasSent() {
		return this.os.toString();
	}

	public void waitUntilRead() {
		await().forever().pollDelay(ofMillis(10)).untilAtomic(bytesNotYetRead, is(0));
	}

	public Adder onReceive(String whenReceived) {
		return new Adder(whenReceived);
	}

	public RegexAdder onReceive(Pattern pattern) {
		return new RegexAdder(pattern);
	}

}
