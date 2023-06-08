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

package org.ardulink.core.qos;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.stream.IntStream.rangeClosed;
import static org.ardulink.util.Throwables.propagate;

import java.io.Closeable;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ardulink.core.StreamReader;
import org.ardulink.util.ByteArray;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArduinoDouble implements Closeable {

	private static final String NO_RESPONSE = null;

	interface ReponseGenerator {

		boolean matches(String received);

		String getResponse();

	}

	public class Adder implements ReponseGenerator {

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

	public Matcher matcher;

	public class RegexAdder implements ReponseGenerator {

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
			this.thenRespond = NO_RESPONSE;
			data.add(this);
		}

		@Override
		public boolean matches(String received) {
			matcher = whenReceived.matcher(received);
			return matcher.matches();
		}

		@Override
		public String getResponse() {
			return thenRespond == null ? null : String.format(thenRespond, groupValues());
		}

		private Object[] groupValues() {
			return rangeClosed(1, matcher.groupCount()).mapToObj(matcher::group).toArray(Object[]::new);
		}

	}

	public class ExecRunnableThenDoBuilder {

		private final Runnable runnable;

		public ExecRunnableThenDoBuilder(Runnable runnable) {
			this.runnable = runnable;
		}

		public void send(String message) {
			newSingleThreadExecutor().submit(() -> {
				try {
					runnable.run();
					ArduinoDouble.this.send(message);
				} catch (Exception e) {
					throw propagate(e);
				}
			});
		}

	}

	private final List<ReponseGenerator> data = new ArrayList<>();
	private final PipedInputStream is2;
	private final PipedOutputStream os1;
	private StreamReader streamReader;
	private PipedOutputStream os2;

	public ArduinoDouble() throws IOException {
		PipedInputStream is1 = new PipedInputStream();
		os1 = new PipedOutputStream(is1);
		is2 = new PipedInputStream();

		os2 = new PipedOutputStream(is2);
		streamReader = new StreamReader(is1) {

			private final ByteArray bytes = new ByteArray();

			@Override
			protected void received(byte[] bytes) throws Exception {
				this.bytes.append(bytes);
				for (ReponseGenerator generator : data) {
					if (generator.matches(new String(this.bytes.copy()))) {
						String response = generator.getResponse();
						if (response != NO_RESPONSE) {
							send(response);
							os2.flush();
						}
						this.bytes.clear();
					}
				}

			}

		};
		streamReader.runReaderThread();
	}

	public void send(String message) throws IOException {
		os2.write(message.getBytes());
	}

	public PipedOutputStream getOutputStream() {
		return os1;
	}

	public PipedInputStream getInputStream() {
		return is2;
	}

	public Adder whenReceive(String whenReceived) {
		return new Adder(whenReceived);
	}

	public RegexAdder whenReceive(Pattern pattern) {
		return new RegexAdder(pattern);
	}

	public ExecRunnableThenDoBuilder after(int amount, TimeUnit timeUnit) {
		return new ExecRunnableThenDoBuilder(() -> {
			try {
				timeUnit.sleep(amount);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
	}

	@Override
	public void close() throws IOException {
		is2.close();
		os1.close();
		streamReader.close();
		os2.close();
	}

}