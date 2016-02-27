package org.zu.ardulink.gui.hamcrest;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JSpinner;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class NumberRowMatcher extends TypeSafeMatcher<JPanel> {

	private final Number value;
	private final BaseBuilder baseBuilder;

	public NumberRowMatcher(BaseBuilder baseBuilder, Number number) {
		this.baseBuilder = baseBuilder;
		this.value = number;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Label ").appendText(baseBuilder.getLabel())
				.appendText(", value ").appendText(String.valueOf(value));
	}

	@Override
	protected void describeMismatchSafely(JPanel item,
			Description mismatchDescription) {
		mismatchDescription.appendText(Arrays.toString(item.getComponents()));
	}

	@Override
	protected boolean matchesSafely(JPanel jPanel) {
		return baseBuilder.labelMatch(jPanel) && valueEq(jPanel)
				&& isNumOnly(jPanel);
	}

	private boolean valueEq(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		return component instanceof JSpinner
				&& value.equals((Number) ((JSpinner) component).getValue());
	}

	private boolean isNumOnly(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		return component instanceof JSpinner;
	}
}
