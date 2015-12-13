package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStopListening;

public class DefaultToArduinoStopListening implements ToArduinoStopListening {

	private final Pin pin;

	public DefaultToArduinoStopListening(Pin pin) {
		this.pin = pin;
	}

	@Override
	public Pin getPin() {
		return pin;
	}

}
