package com.github.pfichtner.hamcrest;

import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.github.pfichtner.Pin.DigitalPin;
import com.github.pfichtner.events.DigitalPinValueChangedEvent;

public class DigitalPinValueChangedEventMatcher extends
		TypeSafeMatcher<Collection<DigitalPinValueChangedEvent>> {

	private DigitalPin digitalPin;
	private Boolean value;

	public DigitalPinValueChangedEventMatcher(DigitalPin digitalPin) {
		this.digitalPin = digitalPin;
	}

	@Override
	public void describeTo(Description description) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean matchesSafely(
			Collection<DigitalPinValueChangedEvent> items) {
		Iterator<DigitalPinValueChangedEvent> iterator = items.iterator();
		if (!iterator.hasNext()) {
			return false;
		}
		DigitalPinValueChangedEvent event = iterator.next();
		return pinsAreEqual(event) && valuesAreEqual(event)
				&& !iterator.hasNext();
	}

	private boolean valuesAreEqual(DigitalPinValueChangedEvent event) {
		return event.getValue().equals(value);
	}

	private boolean pinsAreEqual(DigitalPinValueChangedEvent event) {
		return event.getPin().pinNum() == digitalPin.pinNum();
	}

	public DigitalPinValueChangedEventMatcher withValue(boolean value) {
		this.value = value;
		return this;
	}

	public static DigitalPinValueChangedEventMatcher digitalEvent(
			DigitalPin digitalPin) {
		return new DigitalPinValueChangedEventMatcher(digitalPin);
	}

}
