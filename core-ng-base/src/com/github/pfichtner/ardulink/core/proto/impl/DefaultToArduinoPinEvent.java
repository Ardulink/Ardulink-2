package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoPinEvent;

public class DefaultToArduinoPinEvent implements ToArduinoPinEvent {

	private final Pin pin;
	private final Object value;

	public DefaultToArduinoPinEvent(DigitalPin pin, Boolean value) {
		this.pin = pin;
		this.value = value;
	}

	public DefaultToArduinoPinEvent(AnalogPin pin, Integer value) {
		this.pin = pin;
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
