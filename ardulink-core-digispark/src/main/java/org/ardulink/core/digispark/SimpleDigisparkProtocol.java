package org.ardulink.core.digispark;

import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.util.Preconditions.checkState;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.core.Pin.Type;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.ProtocolNG;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.util.MapBuilder;

public class SimpleDigisparkProtocol implements Protocol, ProtocolNG {

	private enum Message {
		POWER_PIN_INTENSITY((byte) 11) {
			@Override
			public byte getValue(ToDeviceMessagePinStateChange pinStateChange) {
				return ((Integer) pinStateChange.getValue()).byteValue();
			}
		},
		POWER_PIN_SWITCH((byte) 12) {
			@Override
			public byte getValue(ToDeviceMessagePinStateChange pinStateChange) {
				return (byte) (Boolean.TRUE.equals(pinStateChange.getValue()) ? 1 : 0);
			}
		};

		private final byte protoInt;

		private Message(byte protoInt) {
			this.protoInt = protoInt;
		}

		public abstract byte getValue(ToDeviceMessagePinStateChange pinStateChange);
	}

	private final byte separator = (byte) 255;

	private final byte[] separatorArray = new byte[] { separator };

	private static final SimpleDigisparkProtocol instance = new SimpleDigisparkProtocol();

	private static final Map<Type, Message> messages = Collections
			.unmodifiableMap(new EnumMap<Type, Message>(MapBuilder
					.<Type, Message> newMapBuilder()
					.put(ANALOG, Message.POWER_PIN_INTENSITY)
					.put(DIGITAL, Message.POWER_PIN_SWITCH).build()));

	public static Protocol instance() {
		return instance;
	}

	@Override
	public String getName() {
		return SimpleDigisparkProtocol.class.getSimpleName();
	}

	@Override
	public byte[] getSeparator() {
		return separatorArray;
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStartListening startListening) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStopListening stopListening) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
		Pin pin = pinStateChange.getPin();
		Message message = getMappedMessage(pin);
		return new byte[] { message.protoInt, (byte) pin.pinNum(),
				message.getValue(pinStateChange), separator };
	}

	private Message getMappedMessage(Pin pin) {
		Message message = messages.get(pin.getType());
		checkState(message != null,
				"Unsupported type %s of pin %s. Supported types are %s",
				pin.getType(), pin, messages.keySet());
		return message;
	}

	@Override
	public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageTone tone) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageNoTone noTone) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageCustom custom) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FromDeviceMessage fromDevice(byte[] bytes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteStreamProcessor newByteStreamProcessor() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
