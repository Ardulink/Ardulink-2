package com.github.pfichtner.events;

import com.github.pfichtner.Pin.DigitalPin;

public interface DigitalPinValueChangedEvent {

	DigitalPin getPin();

	Boolean getValue();

}
