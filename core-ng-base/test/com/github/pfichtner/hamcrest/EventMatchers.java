package com.github.pfichtner.hamcrest;

import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.events.PinValueChangedEvent;

public class EventMatchers {

	public static PinValueChangedEventMatcher eventFor(Pin pin) {
		return new PinValueChangedEventMatcher(pin);
	}

	public static class PinValueChangedEventMatcher extends
			TypeSafeMatcher<Collection<? extends PinValueChangedEvent>> {

		private final Pin pin;
		private Object value;
		private PinValueChangedEventMatcher next;

		public PinValueChangedEventMatcher(Pin pin) {
			this.pin = pin;
		}

		@Override
		public void describeTo(Description description) {
			pinState(description, pin, value);
		}

		@Override
		protected void describeMismatchSafely(
				Collection<? extends PinValueChangedEvent> item,
				Description description) {
			description = description.appendText("was: ");
			for (Iterator<? extends PinValueChangedEvent> it = item.iterator(); it
					.hasNext();) {
				PinValueChangedEvent event = (PinValueChangedEvent) it.next();
				description = pinState(description, event.getPin(),
						event.getValue());
				if (it.hasNext()) {
					description = description.appendText(" ,");
				}

			}
		}

		private Description pinState(Description description, Pin pin,
				Object value) {
			return description
					.appendText(String.valueOf(pin.getClass().getSimpleName()))
					.appendText("[").appendText(String.valueOf(pin.pinNum()))
					.appendText("]=").appendText(String.valueOf(value));
		}

		@Override
		protected boolean matchesSafely(
				Collection<? extends PinValueChangedEvent> items) {
			return matchesSafely(items.iterator());
		}

		private boolean matchesSafely(
				Iterator<? extends PinValueChangedEvent> iterator) {
			if (!iterator.hasNext()) {
				return false;
			}
			PinValueChangedEvent event = iterator.next();
			if (!pinsAreEqual(event) || !valuesAreEqual(event)) {
				return false;
			}
			return next == null ? !iterator.hasNext() : next
					.matchesSafely(iterator);
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

		public PinValueChangedEventMatcher and(PinValueChangedEventMatcher next) {
			this.next = next;
			return this;
		}

	}

}
