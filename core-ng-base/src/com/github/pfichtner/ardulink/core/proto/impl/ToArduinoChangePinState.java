package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.proto.api.Protocol.FromArduino;

public class ToArduinoChangePinState implements FromArduino {

	private final Pin pin;
	private final Object value;

	public ToArduinoChangePinState(DigitalPin digitalPin, Boolean value) {
		this.pin = digitalPin;
		this.value = value;
	}

	public ToArduinoChangePinState(AnalogPin analogPin, Integer value) {
		this.pin = analogPin;
		this.value = value;
	}

	@Override
	public Pin getPin() {
		return pin;
	}

	@Override
	public Object getValue() {
		return value;
	}

}
