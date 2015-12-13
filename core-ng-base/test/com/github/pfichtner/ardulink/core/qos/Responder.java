package com.github.pfichtner.ardulink.core.qos;

import java.io.InputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zu.ardulink.util.Lists;

import com.github.pfichtner.ardulink.core.StreamReader;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol255;

public class Responder {

	private final static Logger logger = LoggerFactory
			.getLogger(Responder.class);

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

		@Override
		public boolean matches(String received) {
			matcher = whenReceived.matcher(received);
			return matcher.matches();
		}

		@Override
		public String getResponse() {
			return String.format(thenRespond, collectGroups());
		}

		private Object[] collectGroups() {
			int groupCount = matcher.groupCount();
			Object[] groups = new Object[groupCount];
			for (int i = 0; i < groupCount; i++) {
				groups[i] = matcher.group(i + 1);
			}
			return groups;
		}

		public void thenDoNotRespond() {
			// does nothing
		}

	}

	private final List<ReponseGenerator> data = Lists.newArrayList();

	public Responder(InputStream inputStream,
			final PipedOutputStream outputStream, final Protocol protocol) {
		new StreamReader(inputStream) {
			@Override
			protected void received(byte[] bytes) throws Exception {
				String received = new String(bytes);
				logger.info("Received {}", received);

				for (ReponseGenerator generator : data) {
					if (generator.matches(received)) {
						String response = generator.getResponse();
						logger.info("Responding {}", response);
						outputStream.write(response.getBytes());
						outputStream.write(protocol.getSeparator());
						outputStream.flush();
					}else {
						logger.warn("No responder for {}", received);
					}
				}
			}
		}.runReaderThread(new String(ArdulinkProtocol255.instance()
				.getSeparator()));
	}

	public Adder whenReceive(String whenReceived) {
		return new Adder(whenReceived);
	}

	public RegexAdder whenReceive(Pattern pattern) {
		return new RegexAdder(pattern);
	}
}