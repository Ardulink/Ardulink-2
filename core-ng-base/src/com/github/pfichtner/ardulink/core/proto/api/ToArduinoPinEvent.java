package com.github.pfichtner.ardulink.core.proto.api;

import com.github.pfichtner.ardulink.core.Pin;

public class ToArduinoPinEvent {

	public final Pin pin;
	public final Object value;

	public ToArduinoPinEvent(Pin pin, Object value) {
		this.pin = pin;
		this.value = value;
	}

}
