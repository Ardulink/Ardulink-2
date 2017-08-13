package org.ardulink.mqtt.camel;

import static org.ardulink.util.Preconditions.checkState;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.ardulink.mqtt.Config;

public final class FromSimpleProtocol implements Processor {

	private final Config config;

	public FromSimpleProtocol(Config config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		String body = in.getBody(String.class);

		if (body.charAt(0) == 'A') {
			String[] split = body.split("\\=");
			checkState(split.length == 2, "Could not split %s into two parts",
					body);
			String pattern = config.getTopicPatternAnalogRead();
			
			in.setHeader("topic", String.format(pattern, split[0].substring("A".length())));
			in.setBody(split[1]);

			return;
		}
		throw new IllegalStateException("Cannot handle " + body);

	}
}