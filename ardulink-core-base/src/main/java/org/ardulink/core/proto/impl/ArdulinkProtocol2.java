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
import static org.ardulink.core.messages.api.FromDeviceChangeListeningState.Mode.START;
import static org.ardulink.core.messages.api.FromDeviceChangeListeningState.Mode.STOP;
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
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.net.URI;
import java.util.Map;

import org.ardulink.core.Pin;
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
import org.ardulink.core.proto.api.ProtocolNG;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractState;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey;
import org.ardulink.util.Bytes;
import org.ardulink.util.MapBuilder;
import org.ardulink.util.URIs;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkProtocol2 implements Protocol, ProtocolNG {

	private static final String name = "ardulink2";
	private static final byte[] separator = "\n".getBytes();

	private static final ArdulinkProtocol2 instance = new ArdulinkProtocol2();

	private final ALPByteStreamProcessor delegateUntilRefactored = new ALPByteStreamProcessor();

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
	public FromDeviceMessage fromDevice(byte[] bytes) {
		String in = new String(bytes);
		// Matcher matcher = pattern.matcher(in);

		URI uri = URIs.newURI(in);

		String prefix = uri.getScheme();
		checkState("alp".equals(checkNotNull(prefix, "Message %s has no prefix", in)),
				"Expected message prefix to be %s but was %s", "alp", prefix);

		String command = checkNotNull(uri.getHost(), "Message hasn't a command");
		String specs = removeFirstSlash(checkNotNull(uri.getPath(), "Message hasn't specs"));
		String query = uri.getQuery();

		ALPProtocolKey key = ALPProtocolKey.fromString(command).getOrThrow("command %s not known", command);

		if (key == READY) {
			return new DefaultFromDeviceMessageReady();
		} else if (key == RPLY) {
			Map<String, String> params = paramsToMap(query);
			String id = checkNotNull(params.get("id"), "Reply message needs for mandatory param: id");
			return new DefaultFromDeviceMessageReply("ok".equalsIgnoreCase(specs), parseLong(id), params);
		} else if (key == CUSTOM_EVENT) {
			return new DefaultFromDeviceMessageCustom(specs);
		}

		if (key == START_LISTENING_ANALOG) {
			return new DefaultFromDeviceChangeListeningState(analogPin(parseInt(specs)), START);
		} else if (key == START_LISTENING_DIGITAL) {
			return new DefaultFromDeviceChangeListeningState(digitalPin(parseInt(specs)), START);
		} else if (key == STOP_LISTENING_ANALOG) {
			return new DefaultFromDeviceChangeListeningState(analogPin(parseInt(specs)), STOP);
		} else if (key == STOP_LISTENING_DIGITAL) {
			return new DefaultFromDeviceChangeListeningState(digitalPin(parseInt(specs)), STOP);
		}

		String pinAndState = specs;
		String[] split = pinAndState.split("\\/");
		checkState(split.length == 2, "Error splitting %s, cannot process %s", pinAndState, in);

		int pin = parseInt(split[0]);
		int value = parseInt(split[1]);
		if (key == ANALOG_PIN_READ) {
			return new DefaultFromDeviceMessagePinStateChanged(analogPin(pin), value);
		} else if (key == DIGITAL_PIN_READ) {
			return new DefaultFromDeviceMessagePinStateChanged(digitalPin(pin), toBoolean(value));
		}

		throw new IllegalStateException(key + " " + in);
	}

	private static Map<String, String> paramsToMap(String query) {
		MapBuilder<String, String> builder = MapBuilder.<String, String>newMapBuilder();
		for (String param : checkNotNull(query, "Params can't be null").split("&")) {
			String[] kv = param.split("=");
			builder.put(kv[0], kv[1]);
		}
		return builder.build();
	}

	private static String removeFirstSlash(String path) {
		return path.startsWith("/") ? path.substring(1) : path;
	}

	private static IllegalStateException illegalPinType(Pin pin) {
		return new IllegalStateException("Illegal type " + pin.getType() + " of pin " + pin);
	}

	private static Boolean toBoolean(Integer value) {
		return value.intValue() == 1 ? TRUE : FALSE;
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
					ALPProtocolKey key = ALPProtocolKey.fromString(command).getOrThrow("command %s not known", command);
					return RPLY.equals(key) ? new WaitingForOkKo() : new WaitingForPin(key);
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

		private static class WaitingForPin extends AbstractState {
			private ALPProtocolKey protocolKey;

			public WaitingForPin(ALPProtocolKey protocolKey) {
				this.protocolKey = protocolKey;
			}

			@Override
			public AbstractState process(byte b) {
				if (b == '/') {
					return new WaitingForValue(protocolKey, new String(copyOfBuffer()));
				}
				bufferAppend(b);
				return this;
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
				throw new IllegalStateException(command + " " + command);
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

			private FromDeviceMessage message;

			public CommandParsed(FromDeviceMessage message) {
				this.message = message;
			}

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
			if (state instanceof CommandParsed) {
				fireEvent(((CommandParsed) state).message);
			}
			if (state instanceof RplyParsed) {
				fireEvent(((RplyParsed) state).message);
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
			String[] messages = custom.getMessages();
			return toBytes(builder(custom, CUSTOM_MESSAGE).withValues(messages));
		}

		/**
		 * Appends the separator to the passed message. This is not done using string
		 * concatenations but in a byte[] for performance reasons.
		 * 
		 * @param message the message to send
		 * @return byte[] holding the passed message and the protocol's divider
		 */
		private byte[] toBytes(String message) {
			return Bytes.concat(message.getBytes(), ArdulinkProtocol2.separator);
		}

	}

	@Override
	public ByteStreamProcessor newByteStreamProcessor() {
		return new ALPByteStreamProcessor();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStartListening startListening) {
		return delegateUntilRefactored.toDevice(startListening);
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStopListening stopListening) {
		return delegateUntilRefactored.toDevice(stopListening);
	}

	@Override
	public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
		return delegateUntilRefactored.toDevice(pinStateChange);
	}

	@Override
	public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
		return delegateUntilRefactored.toDevice(keyPress);
	}

	@Override
	public byte[] toDevice(ToDeviceMessageTone tone) {
		return delegateUntilRefactored.toDevice(tone);
	}

	@Override
	public byte[] toDevice(ToDeviceMessageNoTone noTone) {
		return delegateUntilRefactored.toDevice(noTone);
	}

	@Override
	public byte[] toDevice(ToDeviceMessageCustom custom) {
		return delegateUntilRefactored.toDevice(custom);
	}

}
