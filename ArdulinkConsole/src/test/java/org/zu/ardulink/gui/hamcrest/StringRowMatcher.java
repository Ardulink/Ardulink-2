package org.zu.ardulink.gui.hamcrest;

import static org.zu.ardulink.gui.hamcrest.RowMatcherBuilder.componentsOf;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

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
		List<? extends Component> componentsOfSubPanel = componentsOf(jPanel);
		return baseBuilder.labelMatch(jPanel) && valueEq(componentsOfSubPanel)
				&& isNumOnly(componentsOfSubPanel);
	}

	private boolean valueEq(List<? extends Component> componentsOfSubPanel) {
		Component component = componentsOfSubPanel
				.get(baseBuilder.getRow() * 2 + 1);
		return component instanceof JTextField
				&& value.equals((String) ((JTextField) component).getText());
	}

	private boolean isNumOnly(List<? extends Component> componentsOfSubPanel) {
		Component component = componentsOfSubPanel
				.get(baseBuilder.getRow() * 2 + 1);
		return component instanceof JTextField;
	}
}
