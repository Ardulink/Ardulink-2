package org.zu.ardulink.gui.hamcrest;

import static org.zu.ardulink.gui.hamcrest.RowMatcherBuilder.componentsOf;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

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
		List<? extends Component> componentsOfSubPanel = componentsOf(jPanel);
		return baseBuilder.labelMatch(jPanel) && valueEq(componentsOfSubPanel)
				&& isNumOnly(componentsOfSubPanel);
	}

	private boolean valueEq(List<? extends Component> componentsOfSubPanel) {
		Component component = componentsOfSubPanel
				.get(baseBuilder.getRow() * 2 + 1);
		return component instanceof JSpinner
				&& value.equals((Number) ((JSpinner) component).getValue());
	}

	private boolean isNumOnly(List<? extends Component> componentsOfSubPanel) {
		Component component = componentsOfSubPanel
				.get(baseBuilder.getRow() * 2 + 1);
		return component instanceof JSpinner;
	}
}
