package org.zu.ardulink.gui.hamcrest;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import javax.swing.JPanel;

import org.hamcrest.Matcher;

public class YesNoRowBuilder {

	private BaseBuilder baseBuilder;

	public YesNoRowBuilder(BaseBuilder baseBuilder) {
		this.baseBuilder = baseBuilder;
	}

	public Matcher<JPanel> withValue(boolean value) {
		return new ChoiceRowMatcher(baseBuilder, new Object[] { TRUE, FALSE },
				Boolean.valueOf(value));
	}

}
