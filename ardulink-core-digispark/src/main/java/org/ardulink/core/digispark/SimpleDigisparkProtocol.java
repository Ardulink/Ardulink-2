package org.ardulink.core.digispark;

import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.util.Preconditions.checkState;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.core.Pin.Type;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.ToArduinoCustomMessage;
import org.ardulink.core.proto.api.ToArduinoKeyPressEvent;
import org.ardulink.core.proto.api.ToArduinoNoTone;
import org.ardulink.core.proto.api.ToArduinoPinEvent;
import org.ardulink.core.proto.api.ToArduinoStartListening;
import org.ardulink.core.proto.api.ToArduinoStopListening;
import org.ardulink.core.proto.api.ToArduinoTone;
import org.ardulink.util.MapBuilder;

public class SimpleDigisparkProtocol implements Protocol {

	private enum Message {
		POWER_PIN_INTENSITY((byte) 11) {
			@Override
			public byte getValue(ToArduinoPinEvent pinEvent) {
				return ((Integer) pinEvent.getValue()).byteValue();
			}
		},
		POWER_PIN_SWITCH((byte) 12) {
			@Override
			public byte getValue(ToArduinoPinEvent pinEvent) {
				return (byte) (Boolean.TRUE.equals(pinEvent.getValue()) ? 1 : 0);
			}
		};

		private final byte protoInt;

		private Message(byte protoInt) {
			this.protoInt = protoInt;
		}

		public abstract byte getValue(ToArduinoPinEvent pinEvent);
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
	public byte[] toArduino(ToArduinoStartListening startListeningEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoStopListening stopListeningEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoPinEvent pinEvent) {
		Pin pin = pinEvent.getPin();
		Message message = getMappedMessage(pin);
		return new byte[] { message.protoInt, (byte) pin.pinNum(),
				message.getValue(pinEvent), separator };
	}

	private Message getMappedMessage(Pin pin) {
		Message message = messages.get(pin.getType());
		checkState(message != null,
				"Unsupported type %s of pin %s. Supported types are %s",
				pin.getType(), pin, messages.keySet());
		return message;
	}

	@Override
	public byte[] toArduino(ToArduinoKeyPressEvent charEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoTone tone) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoNoTone noTone) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoCustomMessage customMessage) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FromArduino fromArduino(byte[] bytes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getName();
	}

}
