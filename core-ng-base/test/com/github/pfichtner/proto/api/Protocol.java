package com.github.pfichtner.proto.api;

import com.github.pfichtner.Pin;

public interface Protocol {

	interface FromArduino {
		Pin getPin();

		Object getValue();
	}

	byte[] toArduino(ToArduinoStartListening startListeningEvent);
	
	byte[] toArduino(ToArduinoPinEvent pinEvent);

	byte[] toArduino(ToArduinoCharEvent charEvent);

	FromArduino fromArduino(byte[] bytes);

}
