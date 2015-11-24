package com.github.pfichtner.events;

import com.github.pfichtner.Pin;

public class FilteredEventListenerAdapter extends EventListenerAdapter {

	private final Pin pin;
	private final EventListenerAdapter delegate;

	public FilteredEventListenerAdapter(Pin pin, EventListenerAdapter delegate) {
		this.pin = pin;
		this.delegate = delegate;
	}

	public Pin getPin() {
		return pin;
	}

	public void stateChanged(AnalogPinValueChangedEvent event) {
		if (accept(event)) {
			this.delegate.stateChanged(event);
		}
	}

	public void stateChanged(DigitalPinValueChangedEvent event) {
		if (accept(event)) {
			this.delegate.stateChanged(event);
		}
	}

	private boolean accept(PinValueChangedEvent event) {
		return this.pin.equals(event.getPin());
	}

}
