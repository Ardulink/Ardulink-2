package com.github.pfichtner.events;

import com.github.pfichtner.Pin;

public interface AnalogPinValueChangedEvent {

	Pin getPin();

	Integer getValue();

}
