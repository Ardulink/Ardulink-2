package com.github.pfichtner.ardulink.core.proto.impl;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.github.pfichtner.ardulink.core.Pin.Type.ANALOG;
import static com.github.pfichtner.ardulink.core.Pin.Type.DIGITAL;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.CHAR_PRESSED;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.CUSTOM_MESSAGE;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.NOTONE;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.TONE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.System.arraycopy;
import static org.zu.ardulink.util.Integers.tryParse;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zu.ardulink.util.Longs;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoCustomMessage;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoKeyPressEvent;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoNoTone;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoPinEvent;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStartListening;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStopListening;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoTone;
import com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey;

public class AbstractArdulinkProtocol implements Protocol {

	private static final Pattern pattern = Pattern
			.compile("alp:\\/\\/([a-z]+)/([\\d]+)/([\\d]+)");

	private final String name;
	private final byte[] separator;

	public AbstractArdulinkProtocol(String name, byte[] separator) {
		this.name = name;
		this.separator = separator;
	}

	public String getName() {
		return name;
	};

	@Override
	public byte[] getSeparator() {
		return separator;
	}

	@Override
	public byte[] toArduino(ToArduinoStartListening startListeningEvent) {
		Pin pin = startListeningEvent.pin;
		if (startListeningEvent.pin.is(ANALOG)) {
			return toBytes(alpProtocolMessage(START_LISTENING_ANALOG).forPin(
					pin.pinNum()).withoutValue());
		}
		if (startListeningEvent.pin.is(DIGITAL)) {
			return toBytes(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(
					pin.pinNum()).withoutValue());
		}
		throw illegalPinType(startListeningEvent.pin);
	}

	@Override
	public byte[] toArduino(ToArduinoStopListening stopListeningEvent) {
		Pin pin = stopListeningEvent.pin;
		if (stopListeningEvent.pin.is(ANALOG)) {
			return toBytes(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(
					pin.pinNum()).withoutValue());
		}
		if (stopListeningEvent.pin.is(DIGITAL)) {
			return toBytes(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(
					pin.pinNum()).withoutValue());
		}
		throw illegalPinType(stopListeningEvent.pin);
	}

	@Override
	public byte[] toArduino(ToArduinoPinEvent pinEvent) {
		if (pinEvent.pin.is(ANALOG)) {
			return toBytes(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(
					pinEvent.pin.pinNum()).withValue((Integer) pinEvent.value));
		}
		if (pinEvent.pin.is(DIGITAL)) {
			return toBytes(alpProtocolMessage(POWER_PIN_SWITCH).forPin(
					pinEvent.pin.pinNum()).withState((Boolean) pinEvent.value));
		}
		throw illegalPinType(pinEvent.pin);
	}

	@Override
	public byte[] toArduino(ToArduinoKeyPressEvent charEvent) {
		return toBytes(alpProtocolMessage(CHAR_PRESSED).withValue(
				"chr" + charEvent.keychar + "cod" + charEvent.keycode + "loc"
						+ charEvent.keylocation + "mod"
						+ charEvent.keymodifiers + "mex"
						+ charEvent.keymodifiersex));
	}

	@Override
	public byte[] toArduino(ToArduinoTone toArduinoTone) {
		Long duration = toArduinoTone.tone.getDurationInMillis();
		return toBytes(alpProtocolMessage(TONE).withValue(
				toArduinoTone.tone.getPin().pinNum() + "/"
						+ toArduinoTone.tone.getHertz() + "/"
						+ (duration == null ? -1 : duration.longValue())));
	}

	@Override
	public byte[] toArduino(ToArduinoNoTone noTone) {
		return toBytes(alpProtocolMessage(NOTONE).usingMessageId(
				noTone.messageId).withValue(noTone.analogPin.pinNum()));
	}

	@Override
	public byte[] toArduino(ToArduinoCustomMessage customMessage) {
		String[] messages = customMessage.messages;
		return toBytes(alpProtocolMessage(CUSTOM_MESSAGE).withValues(messages));
	}

	@Override
	public FromArduino fromArduino(byte[] bytes) {
		String in = new String(bytes);
		if (in.startsWith("alp://rply/")) {
			String substring = in.substring("alp://rply/".length());
			String[] split = substring.split("\\?");
			String[] id = split[1].split("\\=");
			Long tryParse2 = Longs.tryParse(id[1]);
			return new FromArduinoReply(split[0].equalsIgnoreCase("ok"),
					tryParse2.longValue());
		}
		Matcher matcher = pattern.matcher(in);
		checkState(matcher.matches() && matcher.groupCount() == 3, "%s", in);
		ALPProtocolKey key = ALPProtocolKey.fromString(matcher.group(1));
		Integer pin = tryParse(matcher.group(2));
		Integer value = tryParse(matcher.group(3));
		checkState(key != null && pin != null && value != null,
				"key %s pin %s value %s", key, pin, value);
		if (key == ANALOG_PIN_READ) {
			return new FromArduinoPinStateChanged(analogPin(pin), value);
		} else if (key == DIGITAL_PIN_READ) {
			return new FromArduinoPinStateChanged(digitalPin(pin),
					toBoolean(value));
		}
		throw new IllegalStateException(key + " " + in);
	}

	private IllegalStateException illegalPinType(Pin pin) {
		return new IllegalStateException("Illegal type of pin " + pin);
	}

	private static Boolean toBoolean(Integer value) {
		return value.intValue() == 1 ? TRUE : FALSE;
	}

	/**
	 * Appends the separator to the passed message. This is not done using
	 * string concatenations but in a byte[] for performance reasons.
	 * 
	 * @param message
	 *            the message to send
	 * @return byte[] holding the passed message and the protocol's divider
	 */
	private byte[] toBytes(String message) {
		byte[] bytes = new byte[message.length() + separator.length];
		byte[] msgBytes = message.getBytes();
		arraycopy(msgBytes, 0, bytes, 0, msgBytes.length);
		arraycopy(separator, 0, bytes, msgBytes.length, separator.length);
		return bytes;
	}

}
