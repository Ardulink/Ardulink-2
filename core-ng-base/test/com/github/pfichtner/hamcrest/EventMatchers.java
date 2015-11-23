package com.github.pfichtner.hamcrest;

import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.github.pfichtner.Pin;
import com.github.pfichtner.events.PinValueChangedEvent;

public class EventMatchers {

	public static PinValueChangedEventMatcher eventFor(Pin pin) {
		return new PinValueChangedEventMatcher(pin);
	}

	public static class PinValueChangedEventMatcher extends
			TypeSafeMatcher<Collection<? extends PinValueChangedEvent>> {

		private Pin pin;
		private Object value;

		public PinValueChangedEventMatcher(Pin pin) {
			this.pin = pin;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(String.valueOf(pin.getClass()
					.getSimpleName() + "[" + pin.pinNum() + "]=" + value));
		}

		@Override
		protected boolean matchesSafely(
				Collection<? extends PinValueChangedEvent> items) {
			Iterator<? extends PinValueChangedEvent> iterator = items
					.iterator();
			if (!iterator.hasNext()) {
				return false;
			}
			PinValueChangedEvent event = iterator.next();
			return pinsAreEqual(event) && valuesAreEqual(event)
					&& !iterator.hasNext();
		}

		private boolean valuesAreEqual(PinValueChangedEvent event) {
			return event.getValue().equals(value);
		}

		private boolean pinsAreEqual(PinValueChangedEvent event) {
			return event.getPin().pinNum() == pin.pinNum();
		}

		public PinValueChangedEventMatcher withValue(int value) {
			this.value = value;
			return this;
		}

		public PinValueChangedEventMatcher withValue(boolean value) {
			this.value = value;
			return this;
		}

	}

}
