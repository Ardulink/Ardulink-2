package com.github.pfichtner.events;

import com.github.pfichtner.Pin.DigitalPin;

public interface DigitalPinValueChangedEvent {

	DigitalPin getPin();

	Object getValue();

}
