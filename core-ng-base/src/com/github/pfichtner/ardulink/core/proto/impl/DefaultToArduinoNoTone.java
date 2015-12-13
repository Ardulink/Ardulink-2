package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoNoTone;

public class DefaultToArduinoNoTone implements ToArduinoNoTone {

	private final AnalogPin analogPin;

	public DefaultToArduinoNoTone(AnalogPin analogPin) {
		this.analogPin = analogPin;
	}

	@Override
	public AnalogPin getAnalogPin() {
		return analogPin;
	}

}
