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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.System.arraycopy;
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
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

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
import org.ardulink.util.Longs;
import org.ardulink.util.URIs;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkProtocol2 implements Protocol {

	// private static final Pattern pattern = Pattern
	// .compile("alp:\\/\\/([a-z]+)\\/([^\\?]*)(?:\\?id=(\\d+)((\\&[a-zA-Z]+\\=[^\\&]+))*)");

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
	public byte[] toArduino(ToArduinoStartListening startListening) {
		Pin pin = startListening.getPin();
		if (startListening.getPin().is(ANALOG)) {
			return toBytes(alpProtocolMessage(START_LISTENING_ANALOG).forPin(
					pin.pinNum()).withoutValue());
		}
		if (startListening.getPin().is(DIGITAL)) {
			return toBytes(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(
					pin.pinNum()).withoutValue());
		}
		throw illegalPinType(startListening.getPin());
	}

	@Override
	public byte[] toArduino(ToArduinoStopListening stopListening) {
		Pin pin = stopListening.getPin();
		if (stopListening.getPin().is(ANALOG)) {
			return toBytes(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(
					pin.pinNum()).withoutValue());
		}
		if (stopListening.getPin().is(DIGITAL)) {
			return toBytes(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(
					pin.pinNum()).withoutValue());
		}
		throw illegalPinType(stopListening.getPin());
	}

	@Override
	public byte[] toArduino(ToArduinoPinEvent pinStateChange) {
		if (pinStateChange.getPin().is(ANALOG)) {
			return toBytes(builder(pinStateChange, POWER_PIN_INTENSITY).forPin(
					pinStateChange.getPin().pinNum()).withValue(
					(Integer) pinStateChange.getValue()));
		}
		if (pinStateChange.getPin().is(DIGITAL)) {
			return toBytes(builder(pinStateChange, POWER_PIN_SWITCH).forPin(
					pinStateChange.getPin().pinNum()).withState(
					(Boolean) pinStateChange.getValue()));
		}
		throw illegalPinType(pinStateChange.getPin());
	}

	private ALProtoBuilder builder(Object event, ALPProtocolKey key) {
		ALProtoBuilder builder = alpProtocolMessage(key);
		return event instanceof MessageIdHolder ? builder
				.usingMessageId(((MessageIdHolder) event).getId()) : builder;
	}

	@Override
	public byte[] toArduino(ToArduinoKeyPressEvent keyPress) {
		return toBytes(builder(keyPress, CHAR_PRESSED).withValue(
				String.format("chr%scod%sloc%smod%smex%s",
						keyPress.getKeychar(), keyPress.getKeycode(),
						keyPress.getKeylocation(), keyPress.getKeymodifiers(),
						keyPress.getKeymodifiersex())));
	}

	@Override
	public byte[] toArduino(ToArduinoTone tone) {
		Long duration = tone.getTone().getDurationInMillis();
		return toBytes(builder(tone, TONE).withValue(
				tone.getTone().getPin().pinNum() + "/"
						+ tone.getTone().getHertz() + "/"
						+ (duration == null ? -1 : duration.longValue())));
	}

	@Override
	public byte[] toArduino(ToArduinoNoTone noTone) {
		return toBytes(builder(noTone, NOTONE).withValue(
				noTone.getAnalogPin().pinNum()));
	}

	@Override
	public byte[] toArduino(ToArduinoCustomMessage custom) {
		String[] messages = custom.getMessages();
		return toBytes(alpProtocolMessage(CUSTOM_MESSAGE).withValues(messages));
	}

	@Override
	public FromArduino fromArduino(byte[] bytes) {
		String in = new String(bytes);
		// Matcher matcher = pattern.matcher(in);

		URI uri = URIs.newURI(in);

		String alpPrefix = uri.getScheme();
		checkState("alp".equals(checkNotNull(alpPrefix,
				"Message hasn't a prefix")),
				"Message prefix isn't equal to alp. It is: %s", alpPrefix);

		String command = checkNotNull(uri.getHost(), "Message hasn't a command");
		String specs = removeFirstSlash(checkNotNull(uri.getPath(),
				"Message hasn't specs"));
		String query = uri.getQuery();

		ALPProtocolKey key = ALPProtocolKey.fromString(command).getOrThrow(
				"command %s not known", command);

		if (key == READY) {
			return new FromArduinoReady();
		} else if (key == RPLY) {

			Map<String, Object> params = getParamsFromQuery(query);

			checkNotNull(params.get("id"),
					"Reply message needs for mandatory param: id");
			String id = (String) params.get("id");

			return new FromArduinoReply("ok".equalsIgnoreCase(specs),
					checkNotNull(Longs.tryParse(id), "%s not a long value", id)
							.longValue(), params);

		} else if (key == ALPProtocolKey.CUSTOM_EVENT) {
			return new FromArduinoCustom(specs);
		}

		String pinAndState = specs;
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

	private Map<String, Object> getParamsFromQuery(String query) {
		checkNotNull(query, "Params can't be null");
		Map<String, Object> retvalue = new HashMap<String, Object>();
		String[] p = query.split("&");
		for (String param : p) {
			int index = param.indexOf("=");
			retvalue.put(param.substring(0, index), param.substring(index + 1));
		}
		return retvalue;
	}

	private String removeFirstSlash(String path) {
		return !path.startsWith("/") ? path : path.substring(1);
	}

	private IllegalStateException illegalPinType(Pin pin) {
		return new IllegalStateException("Illegal type " + pin.getType()
				+ " of pin " + pin);
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
