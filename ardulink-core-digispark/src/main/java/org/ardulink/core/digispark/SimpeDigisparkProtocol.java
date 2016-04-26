package org.ardulink.core.digispark;

import static java.lang.System.arraycopy;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;

import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.ToArduinoCustomMessage;
import org.ardulink.core.proto.api.ToArduinoKeyPressEvent;
import org.ardulink.core.proto.api.ToArduinoNoTone;
import org.ardulink.core.proto.api.ToArduinoPinEvent;
import org.ardulink.core.proto.api.ToArduinoStartListening;
import org.ardulink.core.proto.api.ToArduinoStopListening;
import org.ardulink.core.proto.api.ToArduinoTone;

public class SimpeDigisparkProtocol implements Protocol {

	private final String name = "simple4digispark";
	private final byte[] separator = {(byte)255};

	private static final byte POWER_PIN_INTENSITY_MESSAGE = 11;
	private static final byte POWER_PIN_SWITCH_MESSAGE = 12;
	
	private static final SimpeDigisparkProtocol instance = new SimpeDigisparkProtocol();

	public static Protocol instance() {
		return instance;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] getSeparator() {
		return separator;
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

		byte[] message = new byte[3 + separator.length];
		
		if (pinEvent.getPin().is(ANALOG)) {
			
			message[0] = POWER_PIN_INTENSITY_MESSAGE;
			message[1] = (byte)pinEvent.getPin().pinNum();
			message[2] = ((Integer)pinEvent.getValue()).byteValue();
			
		} else if (pinEvent.getPin().is(DIGITAL)) {
			message[0] = POWER_PIN_SWITCH_MESSAGE;
			message[1] = (byte)pinEvent.getPin().pinNum();
			
			boolean value = (Boolean)pinEvent.getValue();
			if(value == true) {
				message[2] = 1;
			} else {
				message[2] = 0;
			}
		} else {
			throw new IllegalStateException("Illegal type of pin " + pinEvent.getPin());
		}

		arraycopy(separator, 0, message, 3, separator.length);
		
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

}
