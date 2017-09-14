package org.ardulink.mqtt.camel;

import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.util.Collections.unmodifiableList;
import static org.apache.camel.Exchange.ROUTE_STOP;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.Lists.newArrayList;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.model.language.HeaderExpression;
import org.ardulink.core.proto.impl.ALProtoBuilder;
import org.ardulink.mqtt.Topics;
import org.ardulink.util.ListBuilder;
import org.ardulink.util.Optional;

public final class ToArdulinkProtocol implements Processor {

	public interface MessageCreator {
		Optional<String> createMessage(String topic, String value);
	}

	private static abstract class AbstractMessageCreator implements
			MessageCreator {

		private final Pattern pattern;

		public AbstractMessageCreator(Pattern pattern) {
			this.pattern = checkNotNull(pattern, "Pattern must not be null");
		}

		@Override
		public Optional<String> createMessage(String topic, String message) {
			Matcher matcher = this.pattern.matcher(topic);
			if (matcher.matches() && matcher.groupCount() > 0) {
				Optional<Integer> pin = tryParse(matcher.group(1));
				if (pin.isPresent()) {
					return Optional.of(createMessage(pin.get(), message));
				}
			}
			return Optional.absent();
		}

		protected abstract String createMessage(int pin, String message);

	}

	/**
	 * Does handle mqtt messages for digital pins.
	 */
	private static class DigitalMessageCreator extends AbstractMessageCreator {

		public DigitalMessageCreator(Topics topics) {
			super(topics.getTopicPatternDigitalWrite());
		}

		@Override
		protected String createMessage(int pin, String value) {
			return ALProtoBuilder.alpProtocolMessage(DIGITAL_PIN_READ)
					.forPin(pin).withState(Boolean.parseBoolean(value));
		}

	}

	/**
	 * Does handle mqtt messages for analog pins.
	 */
	private static class AnalogMessageCreator extends AbstractMessageCreator {

		public AnalogMessageCreator(Topics topics) {
			super(topics.getTopicPatternAnalogWrite());
		}

		@Override
		protected String createMessage(int pin, String value) {
			return ALProtoBuilder.alpProtocolMessage(ANALOG_PIN_READ)
					.forPin(pin).withValue(parseInt(value));
		}
	}

	/**
	 * Does handle mqtt messages for controlling Ardulink (start/stop analog
	 * listeners).
	 */
	private static class ControlHandlerAnalog extends AbstractMessageCreator {

		public static class Builder {

			private final Pattern pattern;

			public Builder(Topics topics) {
				this.pattern = topics.getTopicPatternAnalogControl();
			}

			public boolean patternIsValid() {
				return this.pattern != null;
			}

			public ControlHandlerAnalog build() {
				return new ControlHandlerAnalog(this.pattern);
			}

		}

		public ControlHandlerAnalog(Pattern pattern) {
			super(pattern);
		}

		@Override
		protected String createMessage(int pin, String message) {
			ALProtoBuilder builder = parseBoolean(message) ? ALProtoBuilder
					.alpProtocolMessage(START_LISTENING_ANALOG)
					: ALProtoBuilder.alpProtocolMessage(STOP_LISTENING_ANALOG);
			return builder.forPin(pin).withoutValue();
		}
	}

	/**
	 * Does handle mqtt messages for controlling Ardulink (start/stop digital
	 * listeners).
	 */
	public static class ControlHandlerDigital extends AbstractMessageCreator {

		public static class Builder {

			private final Pattern pattern;

			public Builder(Topics topics) {
				this.pattern = topics.getTopicPatternDigitalControl();
			}

			public boolean patternIsValid() {
				return this.pattern != null;
			}

			public ControlHandlerDigital build() {
				return new ControlHandlerDigital(this.pattern);
			}

		}

		public ControlHandlerDigital(Pattern pattern) {
			super(pattern);
		}

		@Override
		protected String createMessage(int pin, String message) {
			ALProtoBuilder builder = parseBoolean(message) ? ALProtoBuilder
					.alpProtocolMessage(START_LISTENING_DIGITAL)
					: ALProtoBuilder.alpProtocolMessage(STOP_LISTENING_DIGITAL);
			return builder.forPin(pin).withoutValue();
		}

	}

	private final List<MessageCreator> creators;
	private ValueBuilder topicFrom = new ValueBuilder(new HeaderExpression(
			"topic"));

	public static ToArdulinkProtocol toArdulinkProtocol(Topics topics) {
		return new ToArdulinkProtocol(topics);
	}

	public ToArdulinkProtocol(Topics topics) {
		this.creators = unmodifiableList(newArrayList(creators(topics)));
	}

	public ToArdulinkProtocol topicFrom(ValueBuilder topicFrom) {
		this.topicFrom = topicFrom;
		return this;
	}

	@Override
	public void process(Exchange exchange) {
		Message in = exchange.getIn();
		String topic = checkNotNull(topicFrom.evaluate(exchange, String.class),
				"topic must not be null");
		String body = checkNotNull(in.getBody(String.class),
				"body must not be null");
		Optional<String> message = createMessage(topic, body);

		if (message.isPresent()) {
			in.setBody(message.getOrThrow(
					"Cannot handle body %s with topic %s", body, topic),
					String.class);
		} else {
			exchange.setProperty(ROUTE_STOP, TRUE);
		}
	}

	private Optional<String> createMessage(String topic, String value) {
		for (MessageCreator creators : this.creators) {
			Optional<String> msg = creators.createMessage(topic, value);
			if (msg.isPresent()) {
				return msg;
			}
		}
		return Optional.absent();
	}

	private List<MessageCreator> creators(Topics topics) {
		ListBuilder<MessageCreator> b = ListBuilder
				.<MessageCreator> newBuilder().addAll(
						new DigitalMessageCreator(topics),
						new AnalogMessageCreator(topics));
		ControlHandlerAnalog.Builder ab = new ControlHandlerAnalog.Builder(
				topics);
		if (ab.patternIsValid()) {
			b = b.add(ab.build());
		}
		ControlHandlerDigital.Builder db = new ControlHandlerDigital.Builder(
				topics);
		if (db.patternIsValid()) {
			b = b.add(db.build());
		}
		return b.build();
	}

}