package com.github.pfichtner.ardulink.core.events;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;

public class DefaultAnalogPinValueChangedEvent implements
		AnalogPinValueChangedEvent {

	private final AnalogPin pin;
	private final Integer value;

	public DefaultAnalogPinValueChangedEvent(AnalogPin pin, Integer value) {
		this.pin = pin;
		this.value = value;
	}

	public AnalogPin getPin() {
		return this.pin;
	}

	public Integer getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "DefaultAnalogPinValueChangedEvent [pin=" + pin + ", value="
				+ value + "]";
	}

}
