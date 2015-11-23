package com.github.pfichtner.proto.api;

import com.github.pfichtner.Pin;
import com.github.pfichtner.proto.impl.ToArduinoCharEvent;
import com.github.pfichtner.proto.impl.ToArduinoPinEvent;

public interface Protocol {

	interface FromArduino {
		Pin getPin();

		Object getValue();
	}

	byte[] toArduino(ToArduinoPinEvent pinEvent);

	byte[] toArduino(ToArduinoCharEvent charEvent);

	FromArduino fromArduino(byte[] bytes);

}
