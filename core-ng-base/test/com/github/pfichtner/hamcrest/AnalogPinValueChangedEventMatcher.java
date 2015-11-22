package com.github.pfichtner.hamcrest;

import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.github.pfichtner.Pin.AnalogPin;
import com.github.pfichtner.events.AnalogPinValueChangedEvent;

public class AnalogPinValueChangedEventMatcher extends
		TypeSafeMatcher<Collection<AnalogPinValueChangedEvent>> {

	private AnalogPin pin;
	private Object value;

	public AnalogPinValueChangedEventMatcher(AnalogPin analogPin) {
		this.pin = analogPin;
	}

	@Override
	public void describeTo(Description description) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean matchesSafely(Collection<AnalogPinValueChangedEvent> items) {
		Iterator<AnalogPinValueChangedEvent> iterator = items.iterator();
		if (!iterator.hasNext()) {
			return false;
		}
		AnalogPinValueChangedEvent event = iterator.next();
		return pinsAreEqual(event) && valuesAreEqual(event)
				&& !iterator.hasNext();
	}

	private boolean valuesAreEqual(AnalogPinValueChangedEvent event) {
		return event.getValue().equals(value);
	}

	private boolean pinsAreEqual(AnalogPinValueChangedEvent event) {
		return event.getPin().pinNum() == pin.pinNum();
	}

	public AnalogPinValueChangedEventMatcher withValue(int value) {
		this.value = value;
		return this;
	}

	public static AnalogPinValueChangedEventMatcher analogEvent(AnalogPin analogPin) {
		return new AnalogPinValueChangedEventMatcher(analogPin);
	}

}
