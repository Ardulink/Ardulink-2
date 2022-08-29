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

import static java.lang.Boolean.TRUE;
import static java.util.Collections.unmodifiableSet;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.Throwables.propagate;
import static org.firmata4j.firmata.parser.FirmataEventType.ANALOG_MESSAGE_RESPONSE;
import static org.firmata4j.firmata.parser.FirmataEventType.DIGITAL_MESSAGE_RESPONSE;
import static org.firmata4j.firmata.parser.FirmataEventType.FIRMWARE_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_CAPABILITIES_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_ID;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_SUPPORTED_MODES;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_VALUE;
import static org.firmata4j.firmata.parser.FirmataToken.END_SYSEX;
import static org.firmata4j.firmata.parser.FirmataToken.REPORT_ANALOG;
import static org.firmata4j.firmata.parser.FirmataToken.REPORT_DIGITAL;
import static org.firmata4j.firmata.parser.FirmataToken.START_SYSEX;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.ardulink.core.Pin;
import org.ardulink.core.Tone;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessageReady;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode;
import org.ardulink.util.anno.LapsedWith;
import org.firmata4j.Consumer;
import org.firmata4j.firmata.FirmataMessageFactory;
import org.firmata4j.firmata.parser.FirmataToken;
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

	// TODO visible for testing
	public static class FirmataPin {

		private int index;
		private int value;
		private final Set<Mode> supportedModes = new CopyOnWriteArraySet<Mode>();
		private final Set<Mode> supportedModes_ = unmodifiableSet(supportedModes);
		private Mode currentMode;

		// TODO visible for testing
		public static enum Mode {
			DIGITAL_INPUT, DIGITAL_OUTPUT, ANALOG_INPUT, PWM, SERVO, SHIFT, I2C, ONEWIRE, STEPPER, ENCODER, SERIAL,
			INPUT_PULLUP,
			// Extended modes
			SPI, SONAR, TONE, DHT;

			public static Mode fromByteValue(byte modeToken) {
				Mode[] values = values();
				return modeToken == FirmataToken.PIN_MODE_IGNORE || modeToken >= values.length ? null
						: values[modeToken];
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

	}

	@Override
	public String getName() {
		return "Firmata";
	}

	private static class FirmataByteStreamProcessor extends AbstractByteStreamProcessor {

		private static final byte[] CAPABILITIES_QUERY = { (byte) 0xF0, (byte) 0x6B, (byte) 0xF7 };

		private static final byte TONE = (byte) 0x5F;
		private static final byte TONE_ON = (byte) 0x00;
		private static final byte TONE_OFF = (byte) 0x01;

		private abstract class PinStateChangedConsumer extends Consumer<Event> {

			@Override
			public void accept(Event evt) {
				String pinString = String.valueOf(evt.getBodyItem(PIN_ID));
				Integer pinInt = tryParse(pinString).getOrThrow("Cannot parse %s", pinString);
				fireEvent(new DefaultFromDeviceMessagePinStateChanged(createPin(pinInt),
						convertValue(evt.getBodyItem(PIN_VALUE))));
			}

			protected abstract Object convertValue(Object value);

			protected abstract Pin createPin(Integer pin);
		}

		private final OutputStream outputStream;
		private final FiniteStateMachine delegate = new FiniteStateMachine(WaitingForMessageState.class);

		public FirmataByteStreamProcessor(OutputStream outputStream) {
			this.outputStream = outputStream;
			delegate.addHandler(ANALOG_MESSAGE_RESPONSE, analogPinStateChangedConsumer());
			delegate.addHandler(DIGITAL_MESSAGE_RESPONSE, digitalPinStateChangedConsumer());
			delegate.addHandler(FIRMWARE_MESSAGE, firmwareConsumer());
			delegate.addHandler(PIN_CAPABILITIES_MESSAGE, capabilitiesConsumer());
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
					fireEvent(new DefaultFromDeviceMessageReady());
					try {
						outputStream.write(CAPABILITIES_QUERY);
					} catch (IOException e) {
						propagate(e);
					}
				}
			};
		}

		private Consumer<Event> capabilitiesConsumer() {
			return new Consumer<Event>() {
				@Override
				public void accept(Event t) {
					byte pinId = (Byte) t.getBodyItem(PIN_ID);
					FirmataPin pin = FirmataPin.fromIndex(pinId);
					for (byte modeByte : (byte[]) t.getBodyItem(PIN_SUPPORTED_MODES)) {
						pin.addSupportedMode(Mode.fromByteValue(modeByte));
					}
					pins.put(pin.index(), pin);
				}
			};
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
			throw notYetImplemented();
		}

		private final Map<Integer, FirmataPin> pins = new ConcurrentHashMap<Integer, FirmataPin>();

		private FirmataPin getPin(int index) {
			@LapsedWith(module = LapsedWith.JDK8, value = "Map#merge")
			FirmataPin firmataPin = pins.get(index);
			if (firmataPin == null) {
				firmataPin = FirmataPin.fromIndex(index);
				pins.put(firmataPin.index(), firmataPin);
			}
			return firmataPin;
		}

		private FirmataPin getPin(Pin pin) {
			return getPin(pin.is(DIGITAL) ? pin.pinNum() + 8 - 2 : pin.pinNum());
		}

		@Override
		public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
			FirmataPin firmataPin = getPin(pinStateChange.getPin());
			if (pinStateChange.getPin().is(ANALOG)) {
				int value = (Integer) pinStateChange.getValue();
				return FirmataMessageFactory.setAnalogPinValue(firmataPin.pinId(), value);
			} else if (pinStateChange.getPin().is(DIGITAL)) {
				return digitalMessage(firmataPin, TRUE.equals(pinStateChange.getValue()));
			}
			throw notYetImplemented();
		}

		private byte[] digitalMessage(FirmataPin pin, boolean value) {
			// have to calculate the value of whole port (8-pin set) the pin sits in
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
			throw notYetImplemented();
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
			Long duration = tone.getDurationInMillis() == null ? 0 : tone.getDurationInMillis();
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
			throw notYetImplemented();
		}

		private static UnsupportedOperationException notYetImplemented() {
			return new UnsupportedOperationException("not yet implemented");
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
	public ByteStreamProcessor newByteStreamProcessor(OutputStream outputStream) {
		return new FirmataByteStreamProcessor(outputStream);
	}

}