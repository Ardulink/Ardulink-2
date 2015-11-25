package com.github.pfichtner.events;

import com.github.pfichtner.Pin.AnalogPin;

public interface AnalogPinValueChangedEvent extends PinValueChangedEvent {

	@Override
	public AnalogPin getPin();

	@Override
	Integer getValue();

}
