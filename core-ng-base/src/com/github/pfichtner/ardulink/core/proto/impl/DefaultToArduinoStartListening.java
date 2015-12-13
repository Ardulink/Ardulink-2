package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStartListening;

public class DefaultToArduinoStartListening implements ToArduinoStartListening {

	private final Pin pin;

	public DefaultToArduinoStartListening(Pin pin) {
		this.pin = pin;
	}

	@Override
	public Pin getPin() {
		return pin;
	}

}
