package com.github.pfichtner.proto.api;

import com.github.pfichtner.Pin;

public interface Protocol {

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
