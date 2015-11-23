package com.github.pfichtner.proto.impl;

import com.github.pfichtner.Pin;

public class ToArduinoPinEvent {

	public final Pin pin;
	public final Object value;

	public ToArduinoPinEvent(Pin pin, Object value) {
		this.pin = pin;
		this.value = value;
	}

}
