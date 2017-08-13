package org.ardulink.mqtt.camel;

import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.unmodifiableList;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.Lists.newArrayList;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.ardulink.mqtt.Config;
import org.ardulink.util.ListBuilder;
import org.ardulink.util.Optional;

final class ToSimpleProtocol implements Processor {

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
				Integer pin = tryParse(matcher.group(1));
				if (pin != null) {
					return Optional.of(createMessage(pin.intValue(), message));
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

		public DigitalMessageCreator(Config config) {
			super(config.getTopicPatternDigitalWrite());
		}

		@Override
		protected String createMessage(int pin, String value) {
			return "D" + pin + "=" + value;
		}

	}

	/**
	 * Does handle mqtt messages for analog pins.
	 */
	private static class AnalogMessageCreator extends AbstractMessageCreator {

		public AnalogMessageCreator(Config config) {
			super(config.getTopicPatternAnalogWrite());
		}

		@Override
		protected String createMessage(int pin, String value) {
			int intensity = checkNotNull(tryParse(value),
					"%s not a valid int value", value).intValue();
			return "A" + pin + "=" + intensity;
		}

	}

	/**
	 * Does handle mqtt messages for controlling Ardulink (start/stop analog
	 * listeners).
	 */
	private static class ControlHandlerAnalog extends AbstractMessageCreator {

		public static class Builder {

			private final Pattern pattern;

			public Builder(Config config) {
				this.pattern = config.getTopicPatternAnalogControl();
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
			return (parseBoolean(message) ? "SL" : "EL") + "=A" + pin;
		}
	}

	/**
	 * Does handle mqtt messages for controlling Ardulink (start/stop digital
	 * listeners).
	 */
	public static class ControlHandlerDigital extends AbstractMessageCreator {

		public static class Builder {

			private final Pattern pattern;

			public Builder(Config config) {
				this.pattern = config.getTopicPatternDigitalControl();
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
			return (parseBoolean(message) ? "SL" : "EL") + "=D" + pin;
		}

	}

	private final List<MessageCreator> creators;

	public ToSimpleProtocol(Config config) {
		this.creators = unmodifiableList(newArrayList(creators(config)));
	}

	@Override
	public void process(Exchange exchange) {
		Message in = exchange.getIn();
		Optional<String> result = createMessage(
				in.getHeader("topic", String.class), in.getBody(String.class));
		if (result.isPresent()) {
			exchange.getOut().setBody(result.get(), String.class);
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

	private List<MessageCreator> creators(Config config) {
		ListBuilder<MessageCreator> b = ListBuilder
				.<MessageCreator> newBuilder().addAll(
						new DigitalMessageCreator(config),
						new AnalogMessageCreator(config));
		ControlHandlerAnalog.Builder ab = new ControlHandlerAnalog.Builder(
				config);
		if (ab.patternIsValid()) {
			b = b.add(ab.build());
		}
		ControlHandlerDigital.Builder db = new ControlHandlerDigital.Builder(
				config);
		if (db.patternIsValid()) {
			b = b.add(db.build());
		}
		return b.build();
	}

}