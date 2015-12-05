package com.github.pfichtner.ardulink.core.proto.api;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;

public class ToArduinoPinEvent {

	public final Pin pin;
	public final Object value;

	public ToArduinoPinEvent(DigitalPin pin, Boolean value) {
		this.pin = pin;
		this.value = value;
	}

	public ToArduinoPinEvent(AnalogPin pin, Integer value) {
		this.pin = pin;
		this.value = value;
	}

}
