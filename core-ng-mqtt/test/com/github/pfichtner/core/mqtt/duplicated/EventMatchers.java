package com.github.pfichtner.core.mqtt.duplicated;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.events.PinValueChangedEvent;

public class EventMatchers {

	public static PinValueChangedEventMatcher eventFor(Pin pin) {
		return new PinValueChangedEventMatcher(pin);
	}

	public static class PinValueChangedEventMatcher extends
			TypeSafeMatcher<PinValueChangedEvent> {

		private final Pin pin;
		private Object value;

		public PinValueChangedEventMatcher(Pin pin) {
			this.pin = pin;
		}

		@Override
		public void describeTo(Description description) {
			pinState(description, pin, value);
		}

		@Override
		protected void describeMismatchSafely(PinValueChangedEvent event,
				Description description) {
			pinState(description.appendText(" was "), event.getPin(),
					event.getValue());
		}

		private Description pinState(Description description, Pin pin,
				Object value) {
			return description.appendText(pin.getClass().getSimpleName())
					.appendText("[").appendValue(pin.pinNum()).appendText("]=")
					.appendValue(value);
		}

		@Override
		protected boolean matchesSafely(PinValueChangedEvent event) {
			return pinsAreEqual(event) && valuesAreEqual(event);
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
