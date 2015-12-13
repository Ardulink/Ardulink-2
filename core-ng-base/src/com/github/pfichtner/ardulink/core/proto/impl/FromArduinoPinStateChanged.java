package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.proto.api.Protocol.FromArduino;

public class FromArduinoPinStateChanged implements FromArduino {

	private final Pin pin;
	private final Object value;

	public FromArduinoPinStateChanged(Pin pin, Object value) {
		this.pin = pin;
		this.value = value;
	}

	public Pin getPin() {
		return this.pin;
	}

	public Object getValue() {
		return this.value;
	}

}
