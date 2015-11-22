package com.github.pfichtner.proto.impl;

import static com.github.pfichtner.Pin.analogPin;
import static com.github.pfichtner.Pin.digitalPin;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.zu.ardulink.util.Integers.tryParse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.pfichtner.Pin;
import com.github.pfichtner.Pin.AnalogPin;
import com.github.pfichtner.Pin.DigitalPin;
import com.github.pfichtner.proto.api.Protocol;
import com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey;

public class ArdulinkProtocol implements Protocol {

	private static final Pattern pattern = Pattern
			.compile("alp:\\/\\/([a-z]+)/([\\d]+)/([\\d]+)");

	@Override
	public byte[] toArduino(ToArduino toArduino) {
		Pin pin = toArduino.getPin();
		if (pin instanceof AnalogPin) {
			return toBytes(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(
					pin.pinNum()).withValue((Integer) toArduino.getValue()));
		}
		if (pin instanceof DigitalPin) {
			return toBytes(alpProtocolMessage(POWER_PIN_SWITCH).forPin(
					pin.pinNum()).withState((Boolean) toArduino.getValue()));
		}
		throw new IllegalStateException(String.valueOf(pin) + " "
				+ toArduino.getValue());
	}

	@Override
	public FromArduino fromArduino(byte[] bytes) {
		Matcher matcher = pattern.matcher(new String(bytes));
		if (matcher.matches() && matcher.groupCount() == 3) {
			ALPProtocolKey key = ALPProtocolKey.fromString(matcher.group(1));
			Integer pin = tryParse(matcher.group(2));
			Integer value = tryParse(matcher.group(3));
			if (key != null && pin != null && value != null) {
				if (key == ANALOG_PIN_READ) {
					return new DefaultFromArduino(analogPin(pin), value);
				} else if (key == DIGITAL_PIN_READ) {
					return new DefaultFromArduino(digitalPin(pin),
							toBoolean(value));
				}
			}
		}
		throw new IllegalStateException(new String(bytes));
	}

	private static Boolean toBoolean(Integer value) {
		return value.intValue() == 1 ? TRUE : FALSE;
	}

	private static byte[] toBytes(String string) {
		return string.getBytes();
	}

}
