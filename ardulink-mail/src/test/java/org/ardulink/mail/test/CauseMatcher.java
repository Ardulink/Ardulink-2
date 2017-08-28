package org.ardulink.mail.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CauseMatcher extends TypeSafeMatcher<Throwable> {

	private final Class<? extends Throwable> type;
	private final Matcher<String> messageMatcher;

	public static CauseMatcher exceptionWithMessage(
			Class<? extends Throwable> type, Matcher<String> messageMatcher) {
		return new CauseMatcher(type, messageMatcher);
	}

	public CauseMatcher(Class<? extends Throwable> type,
			Matcher<String> messageMatcher) {
		this.type = type;
		this.messageMatcher = messageMatcher;
	}

	@Override
	protected boolean matchesSafely(Throwable item) {
		return item.getClass().isAssignableFrom(type)
				&& messageMatcher.matches(item.getMessage());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("expects type ").appendValue(type)
				.appendText(" and a message ").appendValue(messageMatcher);
	}
}