package org.zu.ardulink.gui.hamcrest;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class StringRowMatcher extends TypeSafeMatcher<JPanel> {

	private final String value;
	private final BaseBuilder baseBuilder;

	public StringRowMatcher(BaseBuilder baseBuilder, String value) {
		this.baseBuilder = baseBuilder;
		this.value = value;
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
		return component instanceof JTextField
				&& value.equals((String) ((JTextField) component).getText());
	}

	private boolean isNumOnly(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		return component instanceof JTextField;
	}
}
