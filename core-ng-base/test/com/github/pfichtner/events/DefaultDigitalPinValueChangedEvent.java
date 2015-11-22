package com.github.pfichtner.events;

import com.github.pfichtner.Pin.DigitalPin;

public class DefaultDigitalPinValueChangedEvent implements DigitalPinValueChangedEvent {

	private final DigitalPin digitalPin;
	private final Boolean value;

	public DefaultDigitalPinValueChangedEvent(DigitalPin analogPin, Boolean value) {
		this.digitalPin = analogPin;
		this.value = value;
	}

	public DigitalPin getPin() {
		return this.digitalPin;
	}

	public Boolean getValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((digitalPin == null) ? 0 : digitalPin.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultDigitalPinValueChangedEvent other = (DefaultDigitalPinValueChangedEvent) obj;
		if (digitalPin == null) {
			if (other.digitalPin != null)
				return false;
		} else if (!digitalPin.equals(other.digitalPin))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
