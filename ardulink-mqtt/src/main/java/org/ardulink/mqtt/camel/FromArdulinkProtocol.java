package org.ardulink.mqtt.camel;

import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.util.Preconditions.checkState;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.mqtt.Config;

/**
 * Translates from protocol into the topic using the patterns from
 * {@link Config}.
 */
public final class FromArdulinkProtocol implements Processor {

	private final Protocol protocol = ArdulinkProtocol2.instance();
	private final Config config;
	private String headerNameForTopic = "topic";

	public FromArdulinkProtocol(Config config) {
		this.config = config;
	}

	public FromArdulinkProtocol headerNameForTopic(String headerNameForTopic) {
		this.headerNameForTopic = headerNameForTopic;
		return this;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		FromDeviceMessage deviceMessage = protocol.fromDevice(in.getBody(
				String.class).getBytes());
		checkState(deviceMessage instanceof FromDeviceMessagePinStateChanged,
				"Cannot handle %s", in);
		handle(in, (FromDeviceMessagePinStateChanged) deviceMessage);
	}

	private void handle(Message in,
			FromDeviceMessagePinStateChanged pinChangeEvent) {
		Pin pin = pinChangeEvent.getPin();

		Object value = pinChangeEvent.getValue();
		if (pin.is(DIGITAL)) {
			setHeaderAndBody(in, pin, config.getTopicPatternDigitalRead(),
					value);
		} else if (pin.is(ANALOG)) {
			setHeaderAndBody(in, pin, config.getTopicPatternAnalogRead(), value);
		} else {
			throw new IllegalStateException("Unknown pin type of pin " + pin);
		}

	}

	private void setHeaderAndBody(Message in, Pin pin, String pattern,
			Object value) {
		in.setHeader(headerNameForTopic, String.format(pattern, pin.pinNum()));
		in.setBody(value);
	}

}