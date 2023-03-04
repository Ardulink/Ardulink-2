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
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.CHAR_PRESSED;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.CUSTOM_EVENT;
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

import java.util.Map;
import java.util.Optional;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceChangeListeningState.Mode;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.messages.impl.DefaultFromDeviceChangeListeningState;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessageCustom;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessageReady;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessageReply;
import org.ardulink.core.proto.api.MessageIdHolder;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractState;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey;
import org.ardulink.util.Bytes;
import org.ardulink.util.MapBuilder;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkProtocol2 implements Protocol {

	private static final String name = "ardulink2";
	private static final byte[] separator = "\n".getBytes();

	private static final ArdulinkProtocol2 instance = new ArdulinkProtocol2();

	public static Protocol instance() {
		return instance;
	}

	public String getName() {
		return name;
	};

	private static IllegalStateException illegalPinType(Pin pin) {
		return new IllegalStateException("Illegal type " + pin.getType() + " of pin " + pin);
	}

	public static class ALPByteStreamProcessor extends AbstractByteStreamProcessor {

		private static class WaitingForAlpPrefix extends AbstractState {

			private static final byte[] alp = "alp://".getBytes();

			@Override
			public AbstractState process(byte b) {
				int l = bufferLength();
				if (l <= alp.length && b != alp[l]) {
					return null;
				}
				if (l + 1 == alp.length) {
					return new WaitingForCommand();
				}
				bufferAppend(b);
				return this;
			}

		}

		private static class WaitingForCommand extends AbstractState {
			@Override
			public AbstractState process(byte b) {
				if (b == '/') {
					String command = new String(copyOfBuffer());
					Optional<ALPProtocolKey> optional = ALPProtocolKey.fromString(command);
					if (!optional.isPresent()) {
						return new WaitingForAlpPrefix();
					}
					ALPProtocolKey key = optional.get();
					if (key.equals(READY)) {
						return new ReadyParsed();
					} else if (RPLY.equals(key)) {
						return new WaitingForOkKo();
					} else if (CUSTOM_EVENT.equals(key)) {
						return new WaitingForCustomMessage();
					} else {
						return new WaitingForPin(key);
					}
				}
				bufferAppend(b);
				return this;
			}

		}

		private static class WaitingForOkKo extends AbstractState {

			@Override
			public AbstractState process(byte b) {
				if (b == '?') {
					String string = new String(copyOfBuffer());
					if ("ok".equals(string)) {
						return new WaitForRplyParams(true);
					} else if ("ko".equals(string)) {
						return new WaitForRplyParams(false);
					} else {
						return new WaitingForAlpPrefix();
					}
				}
				bufferAppend(b);
				return this;
			}

		}

		private static class WaitForRplyParams extends AbstractState {

			private final boolean ok;

			public WaitForRplyParams(boolean ok) {
				this.ok = ok;
			}

			@Override
			public AbstractState process(byte b) {
				if (b == '\n') {
					return new RplyParsed(ok, paramsToMap(new String(copyOfBuffer())));
				}
				bufferAppend(b);
				return this;
			}

			private static Map<String, String> paramsToMap(String query) {
				MapBuilder<String, String> builder = MapBuilder.<String, String>newMapBuilder();
				for (String param : checkNotNull(query, "Params can't be null").split("&")) {
					String[] kv = param.split("=");
					builder.put(kv[0], kv[1]);
				}
				return builder.build();
			}

		}

		private static class WaitingForCustomMessage extends AbstractState {

			@Override
			public AbstractState process(byte b) {
				if (b == '\n') {
					return new CustomMessageParsed(new String(copyOfBuffer()));
				}
				bufferAppend(b);
				return this;
			}

		}

		private static class RplyParsed extends AbstractState {

			private final DefaultFromDeviceMessageReply message;

			public RplyParsed(boolean ok, Map<String, String> params) {
				message = new DefaultFromDeviceMessageReply(ok,
						parseLong(checkNotNull(params.remove("id"), "Reply message needs for mandatory param: id")),
						params);
			}

			@Override
			public AbstractState process(byte b) {
				return null;
			}

		}

		private static class CustomMessageParsed extends AbstractState {

			private final DefaultFromDeviceMessageCustom message;

			public CustomMessageParsed(String message) {
				this.message = new DefaultFromDeviceMessageCustom(message);
			}

			@Override
			public AbstractState process(byte b) {
				return null;
			}

		}

		private static class WaitingForPin extends AbstractState {
			private final ALPProtocolKey protocolKey;
			private final boolean hasValue;

			public WaitingForPin(ALPProtocolKey protocolKey) {
				this.protocolKey = protocolKey;
				this.hasValue = !START_LISTENING_ANALOG.equals(protocolKey) //
						&& !STOP_LISTENING_ANALOG.equals(protocolKey) //
						&& !START_LISTENING_DIGITAL.equals(protocolKey) //
						&& !STOP_LISTENING_DIGITAL.equals(protocolKey);
			}

			@Override
			public AbstractState process(byte b) {
				if (b == '\n' && !hasValue) {
					return new CommandParsed(
							new DefaultFromDeviceChangeListeningState(pin(new String(copyOfBuffer())), mode()));
				}
				if (b == '/') {
					return new WaitingForValue(protocolKey, new String(copyOfBuffer()));
				}
				bufferAppend(b);
				return this;
			}

			private Mode mode() {
				return isStarting() ? Mode.START : Mode.STOP;
			}

			private Pin pin(String string) {
				Integer pinNumber = tryParse(string)
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

			private final ALPProtocolKey command;
			private final Pin pin;

			public WaitingForValue(ALPProtocolKey command, String pin) {
				this(command, getPin(command, pin));
			}

			public WaitingForValue(ALPProtocolKey command, Pin pin) {
				this.command = command;
				this.pin = pin;
			}

			@Override
			public AbstractState process(byte b) {
				if (b == '\n') {
					return new CommandParsed(
							new DefaultFromDeviceMessagePinStateChanged(pin, getValue(new String(copyOfBuffer()))));
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

			private Object getValue(String s) {
				int value = parseInt(s);
				if (command == ANALOG_PIN_READ) {
					return value;
				} else if (command == DIGITAL_PIN_READ) {
					return toBoolean(value);
				}
				throw new IllegalStateException(command + " " + this);
			}

			private static Boolean toBoolean(Integer value) {
				return value.intValue() == 1 ? TRUE : FALSE;
			}

		}

		private static class CommandParsed extends AbstractState {

			private final FromDeviceMessage message;

			public CommandParsed(FromDeviceMessage message) {
				this.message = message;
			}

			@Override
			public AbstractState process(byte b) {
				return null;
			}

		}

		private static class ReadyParsed extends AbstractState {

			public final FromDeviceMessage message = new DefaultFromDeviceMessageReady();

			@Override
			public AbstractState process(byte b) {
				return null;
			}

		}

		private AbstractState state;

		@Override
		public void process(byte b) {
			if (state == null) {
				state = new WaitingForAlpPrefix();
			}
			state = state.process(b);
			if (state instanceof ReadyParsed) {
				fireEvent(((ReadyParsed) state).message);
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
			state = null;
		}

		// -- out

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
						.withValue((Integer) pinStateChange.getValue()));
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
					.withValue(String.format("chr%scod%sloc%smod%smex%s", keyPress.getKeychar(), keyPress.getKeycode(),
							keyPress.getKeylocation(), keyPress.getKeymodifiers(), keyPress.getKeymodifiersex())));
		}

		@Override
		public byte[] toDevice(ToDeviceMessageTone tone) {
			Long duration = tone.getTone().getDurationInMillis();
			return toBytes(builder(tone, TONE).withValue(tone.getTone().getPin().pinNum() + "/"
					+ tone.getTone().getHertz() + "/" + (duration == null ? -1 : duration.longValue())));
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
			return Bytes.concat(message.getBytes(), separator);
		}

	}

	@Override
	public ByteStreamProcessor newByteStreamProcessor() {
		return new ALPByteStreamProcessor();
	}

}
