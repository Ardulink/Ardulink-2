package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.proto.api.Protocol.FromArduino;

public class DefaultFromArduino implements FromArduino {

	private final Pin pin;
	private final Object value;

	public DefaultFromArduino(Pin pin, Object value) {
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
