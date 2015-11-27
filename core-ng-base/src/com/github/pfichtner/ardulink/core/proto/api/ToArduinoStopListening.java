package com.github.pfichtner.ardulink.core.proto.api;

import com.github.pfichtner.ardulink.core.Pin;

public class ToArduinoStopListening {

	public final Pin pin;

	public ToArduinoStopListening(Pin pin) {
		this.pin = pin;
	}

}
