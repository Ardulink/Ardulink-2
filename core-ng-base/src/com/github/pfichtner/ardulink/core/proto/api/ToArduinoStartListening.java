package com.github.pfichtner.ardulink.core.proto.api;

import com.github.pfichtner.ardulink.core.Pin;

public class ToArduinoStartListening {

	public final Pin pin;

	public ToArduinoStartListening(Pin pin) {
		this.pin = pin;
	}

}
