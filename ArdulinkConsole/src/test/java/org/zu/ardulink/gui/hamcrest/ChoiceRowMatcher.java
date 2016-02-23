package org.zu.ardulink.gui.hamcrest;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.zu.ardulink.gui.hamcrest.RowMatcherBuilder;

public class ChoiceRowMatcher extends TypeSafeMatcher<JPanel> {

	private final BaseBuilder baseBuilder;
	private final Object[] choices;
	private final Object choice;

	public ChoiceRowMatcher(BaseBuilder baseBuilder, Object[] choices,
			Object choice) {
		this.baseBuilder = baseBuilder;
		this.choices = choices;
		this.choice = choice;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Label ").appendText(baseBuilder.getLabel())
				.appendText(", value ").appendText(String.valueOf(choice))
				.appendText(", choices ").appendText(Arrays.toString(choices));
	}

	@Override
	protected void describeMismatchSafely(JPanel item,
			Description mismatchDescription) {
		mismatchDescription.appendText(Arrays.toString(item.getComponents()));
	}

	@Override
	protected boolean matchesSafely(JPanel jPanel) {
		List<? extends Component> componentsOfSubPanel = RowMatcherBuilder
				.componentsOf(jPanel);
		return baseBuilder.labelMatch(jPanel) && valueEq(componentsOfSubPanel)
				&& choiceEq(componentsOfSubPanel);
	}

	private boolean valueEq(List<? extends Component> componentsOfSubPanel) {
		Component component = componentsOfSubPanel
				.get(baseBuilder.getRow() * 2 + 1);
		return (component instanceof JCheckBox && choice
				.equals(((JCheckBox) component).isSelected()))
				|| (component instanceof JComboBox && String.valueOf(choice)
						.equals(((JComboBox) component).getSelectedItem()));
	}

	private boolean choiceEq(List<? extends Component> componentsOfSubPanel) {
		Component component = componentsOfSubPanel
				.get(baseBuilder.getRow() * 2 + 1);
		return (component instanceof JComboBox)
				|| (component instanceof JCheckBox && Arrays.equals(
						new Object[] { TRUE, FALSE }, choices));
	}

}
