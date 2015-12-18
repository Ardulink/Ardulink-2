package com.github.pfichtner.ardulink.core.events;

import com.github.pfichtner.ardulink.core.Pin.DigitalPin;

public class DefaultDigitalPinValueChangedEvent implements
		DigitalPinValueChangedEvent {

	private final DigitalPin pin;
	private final Boolean value;

	public DefaultDigitalPinValueChangedEvent(DigitalPin pin, boolean value) {
		this.pin = pin;
		this.value = value;
	}

	public DigitalPin getPin() {
		return this.pin;
	}

	public Boolean getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "DefaultDigitalPinValueChangedEvent [pin=" + pin + ", value="
				+ value + "]";
	}

}
