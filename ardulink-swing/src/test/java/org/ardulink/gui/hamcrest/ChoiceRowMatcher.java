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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.ardulink.util.Objects;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
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
		mismatchDescription.appendText(components(item));
	}

	private String components(JPanel item) {
		StringBuilder sb = new StringBuilder();
		for (Component component : item.getComponents()) {
			sb.append(component.getClass().getName()).append("\n");
		}
		return sb.toString();
	}

	@Override
	protected boolean matchesSafely(JPanel jPanel) {
		return baseBuilder.labelMatch(jPanel) && valueEq(jPanel)
				&& choiceEq(jPanel);
	}

	private boolean valueEq(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		return (component instanceof JCheckBox && isCheckBoxEq((JCheckBox) component))
				|| (component instanceof JComboBox && isComboBoxEq((JComboBox) component));
	}

	private boolean isComboBoxEq(JComboBox component) {
		return Arrays.equals(choices, items(component))
				&& Objects.equals(choice,component.getSelectedItem());
	}

	private static Object[] items(JComboBox component) {
		Object[] items = new Object[component.getItemCount()];
		for (int i = 0; i < items.length; i++) {
			items[i] = component.getItemAt(i);
		}
		return items;
	}

	private boolean isCheckBoxEq(JCheckBox component) {
		return choice.equals(component.isSelected());
	}

	private boolean choiceEq(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		return (component instanceof JComboBox)
				|| (component instanceof JCheckBox && Arrays.equals(
						new Object[] { TRUE, FALSE }, choices));
	}

}
