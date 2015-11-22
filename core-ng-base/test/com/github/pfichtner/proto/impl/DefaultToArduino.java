package com.github.pfichtner.proto.impl;

import com.github.pfichtner.Pin;
import com.github.pfichtner.proto.api.Protocol.ToArduino;

public class DefaultToArduino implements ToArduino {

	private final Pin pin;
	private final Object value;

	public DefaultToArduino(Pin pin, Object value) {
		this.pin = pin;
		this.value = value;
	}

	@Override
	public Pin getPin() {
		return this.pin;
	}

	@Override
	public Object getValue() {
		return this.value;
	}

}
