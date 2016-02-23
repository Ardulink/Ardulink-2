package org.zu.ardulink.gui.hamcrest;

import javax.swing.JPanel;

import org.hamcrest.Matcher;

public class ChoiceRowBuilder {

	private final String[] choices;
	private final BaseBuilder baseBuilder;

	public ChoiceRowBuilder(BaseBuilder baseBuilder, String... choices) {
		this.baseBuilder = baseBuilder;
		this.choices = choices;
	}

	public Matcher<JPanel> withValue(String value) {
		return new ChoiceRowMatcher(baseBuilder, choices, value);
	}

}
