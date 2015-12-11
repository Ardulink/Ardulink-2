package com.github.pfichtner.ardulink.core.proto.api;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;

public class ToArduinoNoTone {

	public final AnalogPin analogPin;

	public ToArduinoNoTone(AnalogPin analogPin) {
		this.analogPin = analogPin;
	}

}
