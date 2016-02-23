package org.zu.ardulink.gui.hamcrest;

import static org.zu.ardulink.gui.hamcrest.RowMatcherBuilder.componentsOf;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.hamcrest.Matcher;
import org.zu.ardulink.util.Optional;

public class BaseBuilder {

	private final int row;
	private Optional<String> label = Optional.<String> absent();

	public BaseBuilder(int row) {
		this.row = row;
	}

	public BaseBuilder withLabel(String label) {
		this.label = Optional.of(label);
		return this;
	}

	public Matcher<JPanel> withValue(String value) {
		return new StringRowMatcher(this, value);
	}

	public Matcher<JPanel> withValue(Number number) {
		return new NumberRowMatcher(this, number);
	}

	public ChoiceRowBuilder withChoice(String... choices) {
		return new ChoiceRowBuilder(this, choices);
	}

	public YesNoRowBuilder withYesNo() {
		return new YesNoRowBuilder(this);
	}

	public int getRow() {
		return row;
	}

	public boolean labelMatch(JPanel jPanel) {
		return label.get().equals(
				((JLabel) componentsOf(jPanel).get(getRow() * 2)).getText());
	}

	public String getLabel() {
		return label.get();
	}

}
