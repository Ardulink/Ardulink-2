package com.github.pfichtner.ardulink.core.events;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;

public interface AnalogPinValueChangedEvent extends PinValueChangedEvent {

	@Override
	public AnalogPin getPin();

	@Override
	Integer getValue();

}
