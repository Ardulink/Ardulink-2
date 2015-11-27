package com.github.pfichtner.ardulink.core.events;

import com.github.pfichtner.ardulink.core.Pin.DigitalPin;

public interface DigitalPinValueChangedEvent extends PinValueChangedEvent {

	@Override
	DigitalPin getPin();

	@Override
	Boolean getValue();

}
