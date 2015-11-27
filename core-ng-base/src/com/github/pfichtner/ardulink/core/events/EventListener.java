package com.github.pfichtner.ardulink.core.events;

public interface EventListener {

	void stateChanged(AnalogPinValueChangedEvent event);

	void stateChanged(DigitalPinValueChangedEvent event);

}
