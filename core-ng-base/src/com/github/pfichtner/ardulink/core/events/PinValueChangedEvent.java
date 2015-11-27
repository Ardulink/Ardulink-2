package com.github.pfichtner.ardulink.core.events;

import com.github.pfichtner.ardulink.core.Pin;

public interface PinValueChangedEvent {

	Pin getPin();

	Object getValue();

}
