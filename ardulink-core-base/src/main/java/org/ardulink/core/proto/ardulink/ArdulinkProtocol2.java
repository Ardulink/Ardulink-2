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

package org.ardulink.core.proto.ardulink;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toMap;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.messages.impl.DefaultFromDeviceChangeListeningState.fromDeviceChangeListeningState;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessageCustom.fromDeviceMessageCustom;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessageInfo.fromDeviceMessageInfo;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessagePinStateChanged.fromDeviceMessagePinStateChanged;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessageReply.fromDeviceMessageReply;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.CHAR_PRESSED;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.CUSTOM_EVENT;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.CUSTOM_MESSAGE;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.INFO;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.NOTONE;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.PING;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.RPLY;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.TONE;
import static org.ardulink.util.Booleans.toBoolean;
import static org.ardulink.util.Maps.entry;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Primitives.tryParseAs;

import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceChangeListeningState.Mode;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessageCustom;
import org.ardulink.core.messages.api.FromDeviceMessageReply;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessagePing;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.proto.api.MessageIdHolder;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractState;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.State;
import org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey;
import org.ardulink.util.Bytes;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkProtocol2 implements Protocol {

	public static final String NAME = "ardulink2";
	private static final char NEWLINE = '\n';
	private static final char SLASH = '/';
	private static final byte[] SEPARATOR = new byte[] { NEWLINE };

	@Override
	public String getName() {
		return NAME;
	}

	public static class ALPByteStreamProcessor extends AbstractByteStreamProcessor {

		private static class WaitingForAlpPrefix extends AbstractState {

			private static final byte[] prefix = "alp://".getBytes();

			@Override
			public State process(byte b) {
				int len = bufferLength();
				if (len <= prefix.length && b != prefix[len]) {
					return RESET_STATE;
				}
				if (len + 1 == prefix.length) {
					return new WaitingForCommand();
				}
				bufferAppend(b);
				return this;
			}

		}

		private static class WaitingForCommand extends AbstractState {

			@Override
			public State process(byte b) {
				if (isSlash(b)) {
					return ALPProtocolKey.fromString(bufferAsString()).map(this::toCommand).orElse(RESET_STATE);
				}
				bufferAppend(b);
				return this;
			}

			private State toCommand(ALPProtocolKey key) {
				if (key.equals(INFO)) {
					return new InfoParsed();
				} else if (RPLY.equals(key)) {
					return new WaitingForOkKo();
				} else if (CUSTOM_EVENT.equals(key)) {
					return new WaitingForCustomMessage();
				} else {
					return new WaitingForPin(key);
				}
			}

		}

		private static class WaitingForOkKo extends AbstractState {

			@Override
			public State process(byte b) {
				if (b == '?') {
					if (bufferHasContent("ok")) {
						return new WaitForRplyParams(true);
					} else if (bufferHasContent("ko")) {
						return new WaitForRplyParams(false);
					} else {
						return RESET_STATE;
					}
				}
				bufferAppend(b);
				return this;
			}

		}

		private static class WaitForRplyParams extends AbstractState {

			private final boolean ok;

			private WaitForRplyParams(boolean ok) {
				this.ok = ok;
			}

			@Override
			public State process(byte b) {
				if (isNewline(b)) {
					return new RplyParsed(ok, paramsToMap(bufferAsString()));
				}
				bufferAppend(b);
				return this;
			}

			private static Map<String, String> paramsToMap(String query) {
				return stream(checkNotNull(query, "Params can't be null").split("&")).map(p -> p.split("="))
						.map(kv -> entry(kv[0], kv[1])).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
			}

		}

		private static class WaitingForCustomMessage extends AbstractState {

			@Override
			public State process(byte b) {
				if (isNewline(b)) {
					return new CustomMessageParsed(bufferAsString());
				}
				bufferAppend(b);
				return this;
			}

		}

		private static class RplyParsed extends AbstractState {

			private final FromDeviceMessageReply message;

			private RplyParsed(boolean ok, Map<String, String> params) {
				String key = "id";
				message = fromDeviceMessageReply(ok,
						parseLong(checkNotNull(params.remove(key), "Reply message needs for mandatory param: %s", key)),
						params);
			}

			@Override
			public State process(byte b) {
				return RESET_STATE;
			}

		}

		private static class CustomMessageParsed extends AbstractState {

			private final FromDeviceMessageCustom message;

			private CustomMessageParsed(String message) {
				this.message = fromDeviceMessageCustom(message);
			}

			@Override
			public State process(byte b) {
				return RESET_STATE;
			}

		}

		private static class WaitingForPin extends AbstractState {

			private final ALPProtocolKey protocolKey;
			private final boolean waitForValue;

			private WaitingForPin(ALPProtocolKey protocolKey) {
				this.protocolKey = protocolKey;
				this.waitForValue = !asList(START_LISTENING_ANALOG, STOP_LISTENING_ANALOG, START_LISTENING_DIGITAL,
						STOP_LISTENING_DIGITAL).contains(protocolKey);
			}

			@Override
			public State process(byte b) {
				if (isNewline(b) && !waitForValue) {
					return new CommandParsed(fromDeviceChangeListeningState(pin(bufferAsString()), mode()));
				}
				if (isSlash(b)) {
					return new WaitingForValue(protocolKey, bufferAsString());
				}
				bufferAppend(b);
				return this;
			}

			private Mode mode() {
				return isStarting() ? Mode.START : Mode.STOP;
			}

			private Pin pin(String string) {
				Integer pinNumber = tryParseAs(Integer.class, string)
						.orElseThrow(() -> new IllegalStateException("Cannot parse " + string + " as pin number"));
				return isAnalog() ? analogPin(pinNumber) : digitalPin(pinNumber);
			}

			private boolean isStarting() {
				return START_LISTENING_ANALOG.equals(protocolKey) || START_LISTENING_DIGITAL.equals(protocolKey);
			}

			private boolean isAnalog() {
				return START_LISTENING_ANALOG.equals(protocolKey) //
						|| STOP_LISTENING_ANALOG.equals(protocolKey);
			}

		}

		private static class WaitingForValue extends AbstractState {

			private final Pin pin;

			private WaitingForValue(ALPProtocolKey command, String pin) {
				this(getPin(command, pin));
			}

			private WaitingForValue(Pin pin) {
				this.pin = pin;
			}

			@Override
			public State process(byte b) {
				if (isNewline(b)) {
					return new CommandParsed(fromDeviceMessagePinStateChanged(pin, getValue(bufferAsString())));
				}
				bufferAppend(b);
				return this;
			}

			private static Pin getPin(ALPProtocolKey command, String pin) {
				if (command == ANALOG_PIN_READ) {
					return analogPin(parseInt(pin));
				} else if (command == DIGITAL_PIN_READ) {
					return digitalPin(parseInt(pin));
				}
				throw new IllegalStateException(command + " " + pin);
			}

			private Object getValue(String string) {
				int value = parseInt(string);
				if (pin.is(ANALOG)) {
					return value;
				} else if (pin.is(DIGITAL)) {
					return toBoolean(value);
				}
				throw new IllegalStateException(pin + " " + string);
			}

		}

		private static class CommandParsed extends AbstractState {

			private final FromDeviceMessage message;

			private CommandParsed(FromDeviceMessage message) {
				this.message = message;
			}

			@Override
			public State process(byte b) {
				return RESET_STATE;
			}

		}

		private static class InfoParsed extends AbstractState {

			private final FromDeviceMessage message = fromDeviceMessageInfo();

			@Override
			public State process(byte b) {
				return RESET_STATE;
			}

		}

		protected static final State RESET_STATE = null;

		private State state;

		private static boolean isNewline(byte b) {
			return b == NEWLINE;
		}

		private static boolean isSlash(byte b) {
			return b == SLASH;
		}

		protected static IllegalStateException illegalPinType(Pin pin) {
			return new IllegalStateException("Illegal type " + pin.getType() + " of pin " + pin);
		}

		@Override
		public void process(byte b) {
			state = (state == RESET_STATE ? new WaitingForAlpPrefix() : state).process(b);
			if (state instanceof InfoParsed) {
				fireEvent(((InfoParsed) state).message);
			} else if (state instanceof CommandParsed) {
				fireEvent(((CommandParsed) state).message);
			} else if (state instanceof RplyParsed) {
				fireEvent(((RplyParsed) state).message);
			} else if (state instanceof CustomMessageParsed) {
				fireEvent(((CustomMessageParsed) state).message);
			}
		}

		@Override
		protected void fireEvent(FromDeviceMessage fromDevice) {
			super.fireEvent(fromDevice);
			state = RESET_STATE;
		}

		// -- out

		@Override
		public byte[] toDevice(ToDeviceMessagePing ping) {
			return toBytes(builder(ping, PING).withoutValue());
		}

		@Override
		public byte[] toDevice(ToDeviceMessageStartListening startListening) {
			Pin pin = startListening.getPin();
			if (startListening.getPin().is(ANALOG)) {
				return toBytes(builder(startListening, START_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue());
			}
			if (startListening.getPin().is(DIGITAL)) {
				return toBytes(builder(startListening, START_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue());
			}
			throw illegalPinType(startListening.getPin());
		}

		@Override
		public byte[] toDevice(ToDeviceMessageStopListening stopListening) {
			Pin pin = stopListening.getPin();
			if (stopListening.getPin().is(ANALOG)) {
				return toBytes(builder(stopListening, STOP_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue());
			}
			if (stopListening.getPin().is(DIGITAL)) {
				return toBytes(builder(stopListening, STOP_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue());
			}
			throw illegalPinType(stopListening.getPin());
		}

		@Override
		public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
			if (pinStateChange.getPin().is(ANALOG)) {
				return toBytes(builder(pinStateChange, POWER_PIN_INTENSITY).forPin(pinStateChange.getPin().pinNum())
						.withValue(pinStateChange.getValue()));
			}
			if (pinStateChange.getPin().is(DIGITAL)) {
				return toBytes(builder(pinStateChange, POWER_PIN_SWITCH).forPin(pinStateChange.getPin().pinNum())
						.withState((Boolean) pinStateChange.getValue()));
			}
			throw illegalPinType(pinStateChange.getPin());
		}

		private ALProtoBuilder builder(Object event, ALPProtocolKey key) {
			ALProtoBuilder builder = alpProtocolMessage(key);
			return event instanceof MessageIdHolder ? builder.usingMessageId(((MessageIdHolder) event).getId())
					: builder;
		}

		@Override
		public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
			return toBytes(builder(keyPress, CHAR_PRESSED)
					.withValue(format("chr%scod%sloc%smod%smex%s", keyPress.getKeychar(), keyPress.getKeycode(),
							keyPress.getKeylocation(), keyPress.getKeymodifiers(), keyPress.getKeymodifiersex())));
		}

		@Override
		public byte[] toDevice(ToDeviceMessageTone tone) {
			return toBytes(builder(tone, TONE).withValue(format("%d/%d/%d", tone.getTone().getPin().pinNum(),
					tone.getTone().getHertz(), tone.getTone().getDuration(MILLISECONDS).orElse(-1L))));
		}

		@Override
		public byte[] toDevice(ToDeviceMessageNoTone noTone) {
			return toBytes(builder(noTone, NOTONE).withValue(noTone.getAnalogPin().pinNum()));
		}

		@Override
		public byte[] toDevice(ToDeviceMessageCustom custom) {
			return toBytes(builder(custom, CUSTOM_MESSAGE).withValues(custom.getMessages()));
		}

		/**
		 * Appends the separator to the passed message. This is not done using string
		 * concatenations but in a byte[] for performance reasons.
		 * 
		 * @param message the message to send
		 * @return byte[] holding the passed message and the protocol's divider
		 */
		public byte[] toBytes(String message) {
			return Bytes.concat(message.getBytes(), SEPARATOR);
		}

	}

	@Override
	public ByteStreamProcessor newByteStreamProcessor() {
		return new ALPByteStreamProcessor();
	}

}
