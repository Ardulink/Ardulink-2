package com.github.pfichtner.events;

import com.github.pfichtner.Pin.AnalogPin;

public interface AnalogPinValueChangedEvent {

	AnalogPin getPin();

	Object getValue();

}
