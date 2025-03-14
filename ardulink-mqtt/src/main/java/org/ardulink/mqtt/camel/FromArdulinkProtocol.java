package org.ardulink.mqtt.camel;

import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.ardulink.util.Iterables.getFirst;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.ardulink.ArdulinkProtocol2.ALPByteStreamProcessor;
import org.ardulink.mqtt.Topics;

/**
 * Translates from protocol into the topic using the patterns from
 * {@link Topics}.
 */
public final class FromArdulinkProtocol implements Processor {

	private final ALPByteStreamProcessor byteStreamProcessor = new ALPByteStreamProcessor();
	private final Topics topics;
	private String headerNameForTopic = "topic";

	public static FromArdulinkProtocol fromArdulinkProtocol(Topics topics) {
		return new FromArdulinkProtocol(topics);
	}

	public FromArdulinkProtocol(Topics topics) {
		this.topics = topics;
	}

	public FromArdulinkProtocol headerNameForTopic(String headerNameForTopic) {
		this.headerNameForTopic = checkNotNull(headerNameForTopic, "headerNameForTopic must not be null");
		return this;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		FromDeviceMessage deviceMessage = getFirst(
				parse(byteStreamProcessor, byteStreamProcessor.toBytes(in.getBody(String.class))))
				.orElseThrow(() -> new IllegalStateException("Cannot handle " + in));
		checkState(deviceMessage instanceof FromDeviceMessagePinStateChanged, "Cannot handle %s", in);
		handle(in, (FromDeviceMessagePinStateChanged) deviceMessage);
	}

	private void handle(Message in, FromDeviceMessagePinStateChanged event) {
		Pin pin = event.getPin();
		String topic = String.format(patternFor(pin), pin.pinNum());
		in.setHeader(headerNameForTopic, topic);
		in.setBody(event.getValue());
	}

	private String patternFor(Pin pin) {
		if (pin.is(DIGITAL)) {
			return topics.getTopicPatternDigitalRead();
		} else if (pin.is(ANALOG)) {
			return topics.getTopicPatternAnalogRead();
		} else {
			throw new IllegalStateException("Unknown pin type of pin " + pin);
		}
	}

	@Override
	public String toString() {
		return "FromArdulinkProtocol [protocol=" + byteStreamProcessor + ", topics=" + topics + ", headerNameForTopic="
				+ headerNameForTopic + "]";
	}

}