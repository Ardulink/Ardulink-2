package org.ardulink.mqtt.camel;

import static org.ardulink.util.Preconditions.checkState;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.ardulink.mqtt.Config;
import org.ardulink.util.MapBuilder;

/**
 * Translates from the simple protocol (A3=42 or D4=true) into the topic using
 * the patterns from {@link Config}.
 */
public final class FromSimpleProtocol implements Processor {

	private final Map<String, String> types;

	public FromSimpleProtocol(Config config) {
		types = MapBuilder.<String, String> newMapBuilder()
				.put("A", config.getTopicPatternAnalogRead())
				.put("D", config.getTopicPatternDigitalRead()).build();
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		String body = in.getBody(String.class);

		for (Entry<String, String> entry : types.entrySet()) {
			String type = entry.getKey();
			if (bodyHasType(body, type)) {
				String pattern = entry.getValue();
				setHeaderAndTopic(type, in, pattern);
				return;
			}
		}

		throw new IllegalStateException("Cannot handle " + body);

	}

	private boolean bodyHasType(String body, String type) {
		return body.substring(0, 1).equals(type);
	}

	private void setHeaderAndTopic(String prefix, Message in, String pattern) {
		String[] split = split(in.getBody(String.class));
		in.setHeader("topic",
				String.format(pattern, split[0].substring(prefix.length())));
		in.setBody(split[1]);
	}

	private String[] split(String body) {
		String[] split = body.split("\\=");
		checkState(split.length == 2, "Could not split %s into two parts", body);
		return split;
	}
}