/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ardulink.core.proto.impl;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.CHAR_PRESSED;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.CUSTOM_MESSAGE;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.NOTONE;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.READY;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.RPLY;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.TONE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.System.arraycopy;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ardulink.util.Longs;

import org.ardulink.core.Pin;
import org.ardulink.core.proto.api.MessageIdHolder;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.ToArduinoCustomMessage;
import org.ardulink.core.proto.api.ToArduinoKeyPressEvent;
import org.ardulink.core.proto.api.ToArduinoNoTone;
import org.ardulink.core.proto.api.ToArduinoPinEvent;
import org.ardulink.core.proto.api.ToArduinoStartListening;
import org.ardulink.core.proto.api.ToArduinoStopListening;
import org.ardulink.core.proto.api.ToArduinoTone;
import org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkProtocol2 implements Protocol {

	private static final Pattern pattern = Pattern
			.compile("alp:\\/\\/([a-z]+)\\/([^\\?]*)(?:\\?id=(\\d+))?");

	private final String name = "ardulink2";
	private final byte[] separator = "\n".getBytes();

	private static final ArdulinkProtocol2 instance = new ArdulinkProtocol2();

	public static Protocol instance() {
		return instance;
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
		Pin pin = startListeningEvent.getPin();
		if (startListeningEvent.getPin().is(ANALOG)) {
			return toBytes(alpProtocolMessage(START_LISTENING_ANALOG).forPin(
					pin.pinNum()).withoutValue());
		}
		if (startListeningEvent.getPin().is(DIGITAL)) {
			return toBytes(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(
					pin.pinNum()).withoutValue());
		}
		throw illegalPinType(startListeningEvent.getPin());
	}

	@Override
	public byte[] toArduino(ToArduinoStopListening stopListeningEvent) {
		Pin pin = stopListeningEvent.getPin();
		if (stopListeningEvent.getPin().is(ANALOG)) {
			return toBytes(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(
					pin.pinNum()).withoutValue());
		}
		if (stopListeningEvent.getPin().is(DIGITAL)) {
			return toBytes(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(
					pin.pinNum()).withoutValue());
		}
		throw illegalPinType(stopListeningEvent.getPin());
	}

	@Override
	public byte[] toArduino(ToArduinoPinEvent pinEvent) {
		if (pinEvent.getPin().is(ANALOG)) {
			return toBytes(builder(pinEvent, POWER_PIN_INTENSITY).forPin(
					pinEvent.getPin().pinNum()).withValue(
					(Integer) pinEvent.getValue()));
		}
		if (pinEvent.getPin().is(DIGITAL)) {
			return toBytes(builder(pinEvent, POWER_PIN_SWITCH).forPin(
					pinEvent.getPin().pinNum()).withState(
					(Boolean) pinEvent.getValue()));
		}
		throw illegalPinType(pinEvent.getPin());
	}

	private ALProtoBuilder builder(Object event, ALPProtocolKey key) {
		ALProtoBuilder builder = alpProtocolMessage(key);
		return event instanceof MessageIdHolder ? builder
				.usingMessageId(((MessageIdHolder) event).getId()) : builder;
	}

	@Override
	public byte[] toArduino(ToArduinoKeyPressEvent charEvent) {
		return toBytes(builder(charEvent, CHAR_PRESSED).withValue(
				String.format("chr%scod%sloc%smod%smex%s",
						charEvent.getKeychar(), charEvent.getKeycode(),
						charEvent.getKeylocation(),
						charEvent.getKeymodifiers(),
						charEvent.getKeymodifiersex())));
	}

	@Override
	public byte[] toArduino(ToArduinoTone toArduinoTone) {
		Long duration = toArduinoTone.getTone().getDurationInMillis();
		return toBytes(builder(toArduinoTone, TONE).withValue(
				toArduinoTone.getTone().getPin().pinNum() + "/"
						+ toArduinoTone.getTone().getHertz() + "/"
						+ (duration == null ? -1 : duration.longValue())));
	}

	@Override
	public byte[] toArduino(ToArduinoNoTone noTone) {
		return toBytes(builder(noTone, NOTONE).withValue(
				noTone.getAnalogPin().pinNum()));
	}

	@Override
	public byte[] toArduino(ToArduinoCustomMessage customMessage) {
		String[] messages = customMessage.getMessages();
		return toBytes(alpProtocolMessage(CUSTOM_MESSAGE).withValues(messages));
	}

	@Override
	public FromArduino fromArduino(byte[] bytes) {
		String in = new String(bytes);
		Matcher matcher = pattern.matcher(in);

		checkState(matcher.matches(), "No match %s", in);
		checkState(matcher.groupCount() >= 2, "GroupCount %s",
				matcher.groupCount());
		String command = matcher.group(1);
		ALPProtocolKey key = ALPProtocolKey.fromString(command).getOrThrow(
				"command %s not known", command);

		if (key == READY) {
			return new FromArduinoReady();
		} else if (key == RPLY) {
			checkState(matcher.groupCount() >= 3, "GroupCount %s",
					matcher.groupCount());
			String id = matcher.group(3);
			return new FromArduinoReply(
					matcher.group(2).equalsIgnoreCase("ok"), checkNotNull(
							Longs.tryParse(id), "%s not a long value", id)
							.longValue());
		}

		String pinAndState = matcher.group(2);
		String[] split = pinAndState.split("\\/");
		checkState(split.length == 2, "Error splitting %s, cannot process %s",
				pinAndState, in);

		Integer pin = tryParse(split[0]);
		Integer value = tryParse(split[1]);
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
