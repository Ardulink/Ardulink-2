package com.github.pfichtner.events;

import com.github.pfichtner.Pin;

public interface PinValueChangedEvent {

	Pin getPin();

	Object getValue();

}
