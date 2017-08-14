package org.ardulink.mqtt.camel;

import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;

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
public final class FromSimpleProtocol implements Processor {

	private final Protocol protocol = ArdulinkProtocol2.instance();
	private final Config config;

	public FromSimpleProtocol(Config config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		FromDeviceMessage deviceMessage = protocol.fromDevice(in.getBody(
				String.class).getBytes());
		if (deviceMessage instanceof FromDeviceMessagePinStateChanged) {
			FromDeviceMessagePinStateChanged pinChangeEvent = (FromDeviceMessagePinStateChanged) deviceMessage;
			Pin pin = pinChangeEvent.getPin();
			in.setHeader("topic", String.format(getPattern(pin), pin.pinNum()));
			in.setBody(String.valueOf(pinChangeEvent.getValue()));
			return;
		}
		// TODO throw RTE or NOOP?
		throw new IllegalStateException("Cannot handle " + in);
	}

	private String getPattern(Pin pin) {
		if (pin.is(DIGITAL)) {
			return config.getTopicPatternDigitalRead();
		} else if (pin.is(ANALOG)) {
			return config.getTopicPatternAnalogRead();
		}
		throw new IllegalStateException("Unknown pin type of pin " + pin);
	}
}