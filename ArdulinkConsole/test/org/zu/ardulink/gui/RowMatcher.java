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

package org.zu.ardulink.gui;

import static java.util.Arrays.asList;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class RowMatcher<T> extends TypeSafeMatcher<JPanel> {

	private int row;
	private String label;
	private Object[] choice;
	private Object value;

	public RowMatcher(int row) {
		this.row = row;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Label ").appendText(label)
				.appendText(", value ").appendText(String.valueOf(value))
				.appendText(", choice ").appendText(Arrays.toString(choice));
	}

	@Override
	protected void describeMismatchSafely(JPanel item,
			Description mismatchDescription) {
		mismatchDescription.appendText(Arrays.toString(item.getComponents()));
	}

	@Override
	protected boolean matchesSafely(JPanel jPanel) {
		List<? extends Component> componentsOfSubPanel = componentsOf(jPanel);
		return labelEq(componentsOfSubPanel) && valueEq(componentsOfSubPanel)
				&& choiceEq(componentsOfSubPanel);
	}

	private boolean valueEq(List<? extends Component> componentsOfSubPanel) {
		Component component = componentsOfSubPanel.get(row * 2 + 1);
		boolean b2 = component instanceof JComboBox
				&& String.valueOf(value).equals(
						((JComboBox) component).getSelectedItem());
		boolean b1 = component instanceof JTextField
				&& String.valueOf(value).equals(
						((JTextField) component).getText());
		boolean b3 = component instanceof JCheckBox
				&& Boolean.valueOf((Boolean) value).booleanValue() == ((JCheckBox) component)
						.isSelected();
		return b1 || b2 || b3;
	}

	private boolean choiceEq(List<? extends Component> componentsOfSubPanel) {
		Component component = componentsOfSubPanel.get(row * 2 + 1);
		boolean b1 = component instanceof JComboBox
				&& Arrays.equals(items((JComboBox) component), choice);
		boolean b2 = component instanceof JCheckBox
				&& Arrays.equals(new Object[] { Boolean.TRUE, Boolean.FALSE },
						choice);
		return choice == null || b1 || b2;
	}

	private boolean labelEq(List<? extends Component> componentsOfSubPanel) {
		return label.equals(((JLabel) componentsOfSubPanel.get(row * 2))
				.getText());
	}

	public RowMatcher<T> withLabel(String label) {
		this.label = label;
		return this;
	}

	public RowMatcher<T> withValue(Object value) {
		this.value = value;
		return this;
	}

	public RowMatcher<T> withChoice(Object... choice) {
		this.choice = choice;
		return this;
	}

	public static RowMatcher<JPanel> row(int num) {
		return new RowMatcher<JPanel>(num);
	}

	public static List<? extends Component> componentsOf(JPanel panel) {
		return asList(panel.getComponents());
	}

	public static Object[] items(JComboBox jComboBox) {
		int itemCount = jComboBox.getItemCount();
		Object[] objects = new Object[itemCount];
		for (int i = 0; i < itemCount; i++) {
			objects[i] = jComboBox.getItemAt(i);
		}
		return objects;
	}

}