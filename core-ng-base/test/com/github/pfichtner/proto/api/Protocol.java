package com.github.pfichtner.proto.api;

import com.github.pfichtner.Pin;

public interface Protocol {

	interface ToArduino {
		Pin getPin();

		Object getValue();
	}

	interface FromArduino {
		Pin getPin();

		Object getValue();
	}

	byte[] toArduino(ToArduino toArduino);

	FromArduino fromArduino(byte[] bytes);

}
