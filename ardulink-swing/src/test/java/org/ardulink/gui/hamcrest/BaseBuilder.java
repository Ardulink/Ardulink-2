/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.ardulink.gui.hamcrest;

import static org.ardulink.gui.hamcrest.RowMatcherBuilder.componentsOf;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.hamcrest.Matcher;
import org.ardulink.util.Optional;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class BaseBuilder {

	private static final int ELEMENTS_PER_ROW = 3;
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

	public ChoiceRowBuilder<Object> withChoice(Object... choices) {
		return new ChoiceRowBuilder<Object>(this, choices);
	}

	public YesNoRowBuilder withYesNo() {
		return new YesNoRowBuilder(this);
	}

	public JLabel getLabel(JPanel jPanel) {
		return (JLabel) componentsOf(jPanel).get(base());
	}

	public Component getComponent(JPanel jPanel) {
		return componentsOf(jPanel).get(base() + 1);
	}

	private int base() {
		return row * ELEMENTS_PER_ROW;
	}

	public boolean labelMatch(JPanel jPanel) {
		return label.get().equals(getLabel(jPanel).getText());
	}

	public String getLabel() {
		return label.get();
	}

}
