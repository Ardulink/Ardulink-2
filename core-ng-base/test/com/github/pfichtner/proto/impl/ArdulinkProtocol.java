package com.github.pfichtner.proto.impl;

import static com.github.pfichtner.Pin.analogPin;
import static com.github.pfichtner.Pin.digitalPin;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.zu.ardulink.util.Integers.tryParse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.pfichtner.Pin.AnalogPin;
import com.github.pfichtner.Pin.DigitalPin;
import com.github.pfichtner.proto.api.Protocol;
import com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey;

public class ArdulinkProtocol implements Protocol {

	private static final Pattern pattern = Pattern
			.compile("alp:\\/\\/([a-z]+)/([\\d]+)/([\\d]+)");

	@Override
	public byte[] toArduino(ToArduinoPinEvent pinEvent) {
		if (pinEvent.pin instanceof AnalogPin) {
			return toBytes(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(
					pinEvent.pin.pinNum()).withValue((Integer) pinEvent.value));
		}
		if (pinEvent.pin instanceof DigitalPin) {
			return toBytes(alpProtocolMessage(POWER_PIN_SWITCH).forPin(
					pinEvent.pin.pinNum()).withState((Boolean) pinEvent.value));
		}
		throw new IllegalStateException("Illegal Pin type " + pinEvent.pin);
	}

	@Override
	public byte[] toArduino(ToArduinoCharEvent charEvent) {
		return toBytes(alpProtocolMessage(CHAR_PRESSED).withValue(
				"chr" + charEvent.keychar + "cod" + charEvent.keycode + "loc"
						+ charEvent.keylocation + "mod"
						+ charEvent.keymodifiers + "mex"
						+ charEvent.keymodifiersex));
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
