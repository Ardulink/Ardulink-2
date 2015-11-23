package com.github.pfichtner.events;

import com.github.pfichtner.Pin.DigitalPin;

public interface DigitalPinValueChangedEvent extends PinValueChangedEvent {

	@Override
	DigitalPin getPin();

	@Override
	Boolean getValue();

}
