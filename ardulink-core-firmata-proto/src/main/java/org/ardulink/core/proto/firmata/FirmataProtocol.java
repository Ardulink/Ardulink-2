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

package org.ardulink.core.proto.firmata;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.IntStream.range;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.messages.impl.DefaultFromDeviceChangeListeningState.fromDeviceChangeListeningState;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessageCustom.fromDeviceMessageCustom;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessageInfo.fromDeviceMessageInfo;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessageReply.fromDeviceMessageReply;
import static org.ardulink.core.proto.firmata.FirmataProtocol.FirmataPin.Mode.PWM;
import static org.ardulink.util.Maps.stringToMap;
import static org.ardulink.util.Primitives.tryParseAs;
import static org.firmata4j.firmata.parser.FirmataEventType.ANALOG_MESSAGE_RESPONSE;
import static org.firmata4j.firmata.parser.FirmataEventType.DIGITAL_MESSAGE_RESPONSE;
import static org.firmata4j.firmata.parser.FirmataEventType.FIRMWARE_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_CAPABILITIES_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_ID;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_SUPPORTED_MODES;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_VALUE;
import static org.firmata4j.firmata.parser.FirmataEventType.STRING_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataEventType.SYSEX_CUSTOM_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataToken.CAPABILITY_QUERY;
import static org.firmata4j.firmata.parser.FirmataToken.END_SYSEX;
import static org.firmata4j.firmata.parser.FirmataToken.PIN_MODE_IGNORE;
import static org.firmata4j.firmata.parser.FirmataToken.REPORT_ANALOG;
import static org.firmata4j.firmata.parser.FirmataToken.REPORT_DIGITAL;
import static org.firmata4j.firmata.parser.FirmataToken.REPORT_VERSION;
import static org.firmata4j.firmata.parser.FirmataToken.START_SYSEX;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardulink.core.Pin;
import org.ardulink.core.Pin.Type;
import org.ardulink.core.Tone;
import org.ardulink.core.messages.api.FromDeviceChangeListeningState;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessagePing;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.api.MessageIdHolder;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.firmata.FirmataProtocol.FirmataPin.Mode;
import org.ardulink.util.ByteArray;
import org.firmata4j.Consumer;
import org.firmata4j.firmata.FirmataMessageFactory;
import org.firmata4j.firmata.parser.WaitingForMessageState;
import org.firmata4j.fsm.Event;
import org.firmata4j.fsm.FiniteStateMachine;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class FirmataProtocol implements Protocol {

	public static final String NAME = "Firmata";

	public static class FirmataPin {

		private int index;
		private int value;
		private final Set<Mode> supportedModes = new CopyOnWriteArraySet<Mode>();
		private final Set<Mode> supportedModes_ = unmodifiableSet(supportedModes);
		private Mode currentMode;

		public enum Mode {
			DIGITAL_INPUT, DIGITAL_OUTPUT, ANALOG_INPUT, PWM, SERVO, SHIFT, I2C, ONEWIRE, STEPPER, ENCODER, SERIAL,
			INPUT_PULLUP,
			// Extended modes
			SPI, SONAR, TONE, DHT;

			public static Mode fromByteValue(byte modeToken) {
				Mode[] values = values();
				return modeToken == PIN_MODE_IGNORE || modeToken >= values.length ? null : values[modeToken];
			}
		}

		private FirmataPin(int index) {
			this.index = index;
		}

		public int index() {
			return index;
		}

		public byte portId() {
			return (byte) (index / 8);
		}

		public byte pinId() {
			return (byte) (index % 8);
		}

		public static FirmataPin fromIndex(int index) {
			return new FirmataPin(index);
		}

		public int getValue() {
			return value;
		}

		public void addSupportedMode(Mode mode) {
			if (mode != null) {
				this.supportedModes.add(mode);
			}
		}

		public Set<Mode> getSupportedMode() {
			return this.supportedModes_;
		}

		public boolean modeIs(Mode other) {
			return currentMode == other;
		}

		@Override
		public String toString() {
			return "FirmataPin [index=" + index + ", value=" + value + ", supportedModes=" + supportedModes
					+ ", supportedModes_=" + supportedModes_ + ", currentMode=" + currentMode + "]";
		}

	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	private static class FirmataByteStreamProcessor extends AbstractByteStreamProcessor {

		private static final byte TONE = (byte) 0x5F;
		private static final byte TONE_ON = (byte) 0x00;
		private static final byte TONE_OFF = (byte) 0x01;

		private static final String REPLY_PREFIX = "rply|";
		private static final String LISTEN_PREFIX = "listen|";
		private final AtomicBoolean capabilitiesRequested = new AtomicBoolean(false);
		private volatile OutboundListener outboundListener;

		private abstract class PinStateChangedConsumer extends Consumer<Event> {

			@Override
			public void accept(Event event) {
				String pinString = String.valueOf(event.getBodyItem(PIN_ID));
				Integer pinInt = tryParseAs(Integer.class, pinString).orElseThrow(
						() -> new IllegalStateException(format("Cannot parse %s as pin number", pinString)));
				fireEvent(new DefaultFromDeviceMessagePinStateChanged(createPin(pinInt),
						convertValue(event.getBodyItem(PIN_VALUE))));
			}

			protected abstract Object convertValue(Object value);

			protected abstract Pin createPin(Integer pin);
		}

		private final FiniteStateMachine delegate = new FiniteStateMachine(WaitingForMessageState.class);

		public FirmataByteStreamProcessor() {
			delegate.addHandler(FIRMWARE_MESSAGE, firmwareConsumer());
			delegate.addHandler(PIN_CAPABILITIES_MESSAGE, capabilitiesConsumer());
			delegate.addHandler(ANALOG_MESSAGE_RESPONSE, analogPinStateChangedConsumer());
			delegate.addHandler(DIGITAL_MESSAGE_RESPONSE, digitalPinStateChangedConsumer());
			delegate.addHandler(STRING_MESSAGE, stringMessageConsumer());
			delegate.addHandler(SYSEX_CUSTOM_MESSAGE, sysexCustomMessageConsumer());
		}

		@Override
		public void setOutboundListener(OutboundListener listener) {
			this.outboundListener = listener;
		}

		private void sendOutbound(byte[] bytes) {
			OutboundListener listener = this.outboundListener;
			if (listener != null) {
				listener.outbound(bytes);
			}
		}

		private PinStateChangedConsumer analogPinStateChangedConsumer() {
			return new PinStateChangedConsumer() {

				@Override
				protected Object convertValue(Object value) {
					return value;
				}

				@Override
				protected Pin createPin(Integer pin) {
					return analogPin(pin);
				}

			};
		}

		private PinStateChangedConsumer digitalPinStateChangedConsumer() {
			return new PinStateChangedConsumer() {

				@Override
				protected Object convertValue(Object value) {
					return value.equals(1);
				}

				@Override
				protected Pin createPin(Integer pin) {
					return digitalPin(pin);
				}

			};
		}

		private Consumer<Event> firmwareConsumer() {
			return new Consumer<Event>() {
				@Override
				public void accept(Event t) {
					fireEvent(fromDeviceMessageInfo());
					if (capabilitiesRequested.compareAndSet(false, true)) {
						sendOutbound(new byte[] { START_SYSEX, CAPABILITY_QUERY, END_SYSEX });
					}
				}
			};
		}

		private Consumer<Event> capabilitiesConsumer() {
			return new Consumer<Event>() {
				@Override
				public void accept(Event event) {
					byte index = (Byte) event.getBodyItem(PIN_ID);
					FirmataPin pin = FirmataPin.fromIndex(index);
					byte[] modes = (byte[]) event.getBodyItem(PIN_SUPPORTED_MODES);
					range(0, modes.length).forEach(i -> pin.addSupportedMode(Mode.fromByteValue(modes[i])));
					pins.put(pin.index(), pin);
				}
			};
		}

		private Consumer<Event> stringMessageConsumer() {
			return new Consumer<Event>() {
				@Override
				public void accept(Event event) {
					String message = (String) event.getBodyItem("stringMessage");
					if (message != null) {
						handleStringMessage(message);
					}
				}
			};
		}

		private Consumer<Event> sysexCustomMessageConsumer() {
			return new Consumer<Event>() {
				@Override
				public void accept(Event event) {
					byte[] data = (byte[]) event.getBodyItem("sysexCustomMessage");
					if (data != null) {
						fireEvent(fromDeviceMessageCustom(bytesToString(data)));
					}
				}
			};
		}

		private void handleStringMessage(String message) {
			if (message.startsWith(REPLY_PREFIX)) {
				handleReplyMessage(message);
			} else if (message.startsWith(LISTEN_PREFIX)) {
				handleListenMessage(message);
			} else {
				fireEvent(fromDeviceMessageCustom(message));
			}
		}

		private void handleListenMessage(String message) {
			String payload = message.substring(LISTEN_PREFIX.length());
			String[] parts = payload.split("\\|", -1);
			if (parts.length >= 3) {
				FromDeviceChangeListeningState.Mode mode = "start".equals(parts[0])
						? FromDeviceChangeListeningState.Mode.START
						: FromDeviceChangeListeningState.Mode.STOP;
				boolean isAnalog = "analog".equals(parts[1]);
				Integer pinNum = tryParseAs(Integer.class, parts[2]).orElseThrow(
						() -> new IllegalStateException(format("Cannot parse %s as pin number", parts[2])));
				Pin pin = isAnalog ? analogPin(pinNum) : digitalPin(pinNum);
				fireEvent(fromDeviceChangeListeningState(pin, mode));
			}
		}

		private void handleReplyMessage(String message) {
			String payload = message.substring(REPLY_PREFIX.length());
			String[] parts = payload.split("\\|", 3);
			if (parts.length >= 2) {
				boolean ok = "ok".equals(parts[0]);
				long replyId = tryParseAs(Long.class, parts[1])
						.orElseThrow(() -> new IllegalStateException(format("Cannot parse %s as reply id", parts[1])));
				Map<String, String> params = parts.length == 3 ? stringToMap(parts[2], "\\|", "=") : Map.of();
				fireEvent(fromDeviceMessageReply(ok, replyId, params));
			}
		}

		private static String bytesToString(byte[] data) {
			StringBuilder sb = new StringBuilder();
			int count = data.length / 2;
			for (int i = 0; i < count; i++) {
				sb.append((char) ((data[i * 2] & 0xff) | ((data[i * 2 + 1] & 0xff) << 7)));
			}
			return sb.toString();
		}

		@Override
		public void process(byte[] bytes) {
			delegate.process(bytes);
		}

		@Override
		public void process(byte b) {
			delegate.process(b);
		}

		@Override
		public byte[] toDevice(ToDeviceMessagePing ping) {
			return new byte[] { REPORT_VERSION };
		}

		@Override
		public byte[] toDevice(ToDeviceMessageStartListening startListening) {
			return reportingMessage(startListening.getPin(), true);
		}

		@Override
		public byte[] toDevice(ToDeviceMessageStopListening stopListening) {
			return reportingMessage(stopListening.getPin(), false);
		}

		private byte[] reportingMessage(Pin pin, boolean on) {
			byte portId = getPin(pin).portId();
			byte state = booleanToByte(on);
			if (pin.is(ANALOG)) {
				return new byte[] { (byte) (REPORT_ANALOG | portId), state };
			} else if (pin.is(DIGITAL)) {
				return new byte[] { (byte) (REPORT_DIGITAL | portId), state };
			}
			throw new UnsupportedOperationException("Unsupported pin type: " + pin.getType());
		}

		private final Map<Integer, FirmataPin> pins = new ConcurrentHashMap<Integer, FirmataPin>();

		private FirmataPin getPin(int index) {
			return pins.computeIfAbsent(index, FirmataPin::fromIndex);
		}

		private FirmataPin getPin(Pin pin) {
			return getPin(pin.is(ANALOG) ? 2 * 8 + pin.pinNum() : pin.pinNum());
		}

		@Override
		public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
			ByteArray message = new ByteArray();
			FirmataPin firmataPin = getPin(switchPinType(pinStateChange.getPin(), DIGITAL));
			if (pinStateChange.getPin().is(ANALOG)) {
				if (!firmataPin.modeIs(PWM)) {
					message.append(
							FirmataMessageFactory.setMode((byte) firmataPin.index(), org.firmata4j.Pin.Mode.PWM));
					firmataPin.currentMode = PWM;
				}
				int value = (Integer) pinStateChange.getValue();
				message.append(FirmataMessageFactory.setAnalogPinValue((byte) firmataPin.index(), value));
			} else if (pinStateChange.getPin().is(DIGITAL)) {
				message.append(digitalMessage(firmataPin, TRUE.equals(pinStateChange.getValue())));
			} else {
				throw new UnsupportedOperationException("Unsupported pin type: " + pinStateChange.getPin().getType());
			}
			return message.copy();
		}

		private static Pin switchPinType(Pin pin, Type type) {
			return type == ANALOG ? analogPin(pin.pinNum()) : digitalPin(pin.pinNum());
		}

		private byte[] digitalMessage(FirmataPin pin, boolean value) {
			byte portId = pin.portId();
			byte pinInPort = pin.pinId();
			byte portValue = 0;
			for (int i = 0; i < 8; i++) {
				FirmataPin p = getPin(portId * 8 + i);
				if (p.modeIs(FirmataPin.Mode.DIGITAL_OUTPUT) && p.getValue() > 0) {
					portValue |= 1 << i;
				}
			}
			byte bitmask = (byte) (1 << pinInPort);
			if (value) {
				portValue |= bitmask;
			} else {
				portValue &= ((byte) ~bitmask);
			}
			return FirmataMessageFactory.setDigitalPinValue(portId, portValue);
		}

		private static byte booleanToByte(boolean state) {
			return state ? (byte) 1 : (byte) 0;
		}

		@Override
		public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
			String encoded = String.format("chr%scod%sloc%smod%smex%s", keyPress.getKeychar(), keyPress.getKeycode(),
					keyPress.getKeylocation(), keyPress.getKeymodifiers(), keyPress.getKeymodifiersex());
			return FirmataMessageFactory.stringMessage(withMessageId(keyPress, encoded));
		}

		/**
		 * <a href=
		 * "https://github.com/firmata/protocol/blob/master/proposals/tone-proposal.md">Not
		 * part of Firmata! This is a proposal</a>
		 */
		@Override
		public byte[] toDevice(ToDeviceMessageTone toneMessage) {
			Tone tone = toneMessage.getTone();
			int frequency = tone.getHertz();
			Long duration = tone.getDuration(MILLISECONDS).orElse(0L);
			return sysex(TONE, TONE_ON, (byte) tone.getPin().pinNum(), lsb(frequency), msb(frequency), lsb(duration),
					msb(duration));
		}

		/**
		 * <a href=
		 * "https://github.com/firmata/protocol/blob/master/proposals/tone-proposal.md">Not
		 * part of Firmata! This is a proposal</a>
		 */
		@Override
		public byte[] toDevice(ToDeviceMessageNoTone noToneMessage) {
			return sysex(TONE, TONE_OFF, (byte) noToneMessage.getAnalogPin().pinNum());
		}

		@Override
		public byte[] toDevice(ToDeviceMessageCustom custom) {
			ByteArray result = new ByteArray();
			for (String msg : custom.getMessages()) {
				result.append(FirmataMessageFactory.stringMessage(withMessageId(custom, msg)));
			}
			return result.copy();
		}

		private static String withMessageId(Object message, String payload) {
			if (message instanceof MessageIdHolder) {
				long id = ((MessageIdHolder) message).getId();
				if (id >= 0) {
					return payload + "|" + id;
				}
			}
			return payload;
		}

		private byte[] sysex(byte... bytes) {
			byte[] sysex = new byte[bytes.length + 2];
			sysex[0] = START_SYSEX;
			System.arraycopy(bytes, 0, sysex, 1, bytes.length);
			sysex[sysex.length - 1] = END_SYSEX;
			return sysex;
		}

		private static byte lsb(long value) {
			return mask(value);
		}

		private static byte msb(long value) {
			return mask(shiftBy(value, 1));
		}

		private static long shiftBy(long value, int shiftBy) {
			return value >>> (7 * shiftBy);
		}

		private static byte mask(long value) {
			return (byte) (value & 0x7F);
		}

	}

	@Override
	public ByteStreamProcessor newByteStreamProcessor() {
		return new FirmataByteStreamProcessor();
	}

}