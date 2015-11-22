package com.github.pfichtner.events;

import com.github.pfichtner.Pin.AnalogPin;

public class DefaultAnalogPinValueChangedEvent implements
		AnalogPinValueChangedEvent {

	private final AnalogPin analogPin;
	private final Integer value;

	public DefaultAnalogPinValueChangedEvent(AnalogPin analogPin, Integer value) {
		this.analogPin = analogPin;
		this.value = value;
	}

	public AnalogPin getPin() {
		return this.analogPin;
	}

	public Integer getValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((analogPin == null) ? 0 : analogPin.hashCode());
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
		DefaultAnalogPinValueChangedEvent other = (DefaultAnalogPinValueChangedEvent) obj;
		if (analogPin == null) {
			if (other.analogPin != null)
				return false;
		} else if (!analogPin.equals(other.analogPin))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
