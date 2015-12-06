package com.github.pfichtner.ardulink.core.proto.api;

import com.github.pfichtner.ardulink.core.Pin;

public interface Protocol {

	String getName();

	byte[] getSeparator();

	interface FromArduino {
		Pin getPin();

		Object getValue();
	}

	byte[] toArduino(ToArduinoStartListening startListeningEvent);

	byte[] toArduino(ToArduinoStopListening stopListeningEvent);

	byte[] toArduino(ToArduinoPinEvent pinEvent);

	byte[] toArduino(ToArduinoCharEvent charEvent);

	FromArduino fromArduino(byte[] bytes);

}
