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

import static org.ardulink.util.Bytes.concat;
import static org.ardulink.util.Throwables.propagate;

import java.io.Closeable;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ardulink.core.StreamReader;
import org.ardulink.util.Lists;

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

		public void thenRespond(String thenRespond) {
			this.thenRespond = thenRespond;
			data.add(this);
		}

		public void thenDoNotRespond() {
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
			return thenRespond == null ? null : String.format(thenRespond, collectGroups());
		}

		private Object[] collectGroups() {
			int groupCount = matcher.groupCount();
			Object[] groups = new Object[groupCount];
			for (int i = 0; i < groupCount; i++) {
				groups[i] = matcher.group(i + 1);
			}
			return groups;
		}

	}

	public class WaitThenDoBuilder {

		private int i;
		private TimeUnit timeUnit;

		public WaitThenDoBuilder(int i, TimeUnit timeUnit) {
			this.i = i;
			this.timeUnit = timeUnit;
		}

		public void send(final String message) {
			Executors.newSingleThreadExecutor().submit(new Runnable() {
				@Override
				public void run() {
					try {
						timeUnit.sleep(i);
						ArduinoDouble.this.send(message);
					} catch (Exception e) {
						throw propagate(e);
					}
				}
			});
		}

	}

	private final List<ReponseGenerator> data = Lists.newArrayList();
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
			
			private byte[] bytes = new byte[0];
			
			@Override
			protected void received(byte[] bytes) throws Exception {
				this.bytes = concat(this.bytes, bytes);
				for (ReponseGenerator generator : data) {
					String received = new String(this.bytes);
					if (generator.matches(received)) {
						String response = generator.getResponse();
						if (response != NO_RESPONSE) {
							send(response);
							os2.flush();
						}
						this.bytes = new byte[0];
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

	public WaitThenDoBuilder after(int i, TimeUnit timeUnit) {
		return new WaitThenDoBuilder(i, timeUnit);
	}

	@Override
	public void close() throws IOException {
		is2.close();
		os1.close();
		streamReader.close();
		os2.close();
	}

}