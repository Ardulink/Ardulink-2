package com.github.pfichtner.proto.impl;

import static com.github.pfichtner.Pin.analogPin;
import static com.github.pfichtner.Pin.digitalPin;
import static com.github.pfichtner.Pins.isAnalog;
import static com.github.pfichtner.Pins.isDigital;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.CHAR_PRESSED;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.System.arraycopy;
import static org.zu.ardulink.util.Integers.tryParse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.pfichtner.Pin;
import com.github.pfichtner.proto.api.Protocol;
import com.github.pfichtner.proto.api.ToArduinoCharEvent;
import com.github.pfichtner.proto.api.ToArduinoPinEvent;
import com.github.pfichtner.proto.api.ToArduinoStartListening;
import com.github.pfichtner.proto.api.ToArduinoStopListening;
import com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey;

public class ArdulinkProtocol implements Protocol {

	public static final byte[] READ_DIVIDER = "\n".getBytes();

	private static final Pattern pattern = Pattern
			.compile("alp:\\/\\/([a-z]+)/([\\d]+)/([\\d]+)");

	// TODO refactor all analog/digital switches

	@Override
	public byte[] toArduino(ToArduinoStartListening startListeningEvent) {
		Pin pin = startListeningEvent.pin;
		if (isAnalog(startListeningEvent.pin)) {
			return toBytes(alpProtocolMessage(START_LISTENING_ANALOG).forPin(
					pin.pinNum()).withoutValue());
		}
		if (isDigital(startListeningEvent.pin)) {
			return toBytes(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(
					pin.pinNum()).withoutValue());
		}
		throw new IllegalStateException("Illegal Pin type "
				+ startListeningEvent.pin);
	}

	@Override
	public byte[] toArduino(ToArduinoStopListening stopListeningEvent) {
		Pin pin = stopListeningEvent.pin;
		if (isAnalog(stopListeningEvent.pin)) {
			return toBytes(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(
					pin.pinNum()).withoutValue());
		}
		if (isDigital(stopListeningEvent.pin)) {
			return toBytes(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(
					pin.pinNum()).withoutValue());
		}
		throw new IllegalStateException("Illegal Pin type "
				+ stopListeningEvent.pin);
	}

	@Override
	public byte[] toArduino(ToArduinoPinEvent pinEvent) {
		if (isAnalog(pinEvent.pin)) {
			return toBytes(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(
					pinEvent.pin.pinNum()).withValue((Integer) pinEvent.value));
		}
		if (isDigital(pinEvent.pin)) {
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

	private static byte[] toBytes(String message) {
		byte[] bytes = new byte[message.length() + READ_DIVIDER.length];
		byte[] msgBytes = message.getBytes();
		arraycopy(msgBytes, 0, bytes, 0, msgBytes.length);
		arraycopy(READ_DIVIDER, 0, bytes, msgBytes.length, READ_DIVIDER.length);
		return bytes;
	}

}
