package com.github.pfichtner.events;

public interface EventListener {

	void stateChanged(AnalogPinValueChangedEvent event);

	void stateChanged(DigitalPinValueChangedEvent event);

}
