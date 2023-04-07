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
package org.ardulink.gui.assertj;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;
import java.util.stream.IntStream;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ChoiceRowMatcher {

	private final BaseBuilder baseBuilder;
	private final Object[] choices;
	private final Object choice;

	public ChoiceRowMatcher(BaseBuilder baseBuilder, Object[] choices, Object choice) {
		this.baseBuilder = baseBuilder;
		this.choices = choices;
		this.choice = choice;
	}

	public void verify(JPanel jPanel) {
		baseBuilder.assertLabelMatch(jPanel);
		assertValueEq(jPanel);
		assertChoiceEq(jPanel);
	}

	private void assertValueEq(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		if (component instanceof JCheckBox) {
			assertCheckBoxEq((JCheckBox) component);
		}
		if (component instanceof JComboBox) {
			assertComboBoxEq((JComboBox<?>) component);
		}
	}

	private void assertComboBoxEq(JComboBox<?> component) {
		assertThat(choices).containsExactly(items(component));
		assertThat(choice).isEqualTo(component.getSelectedItem());
	}

	private static Object[] items(JComboBox<?> component) {
		return IntStream.range(0, component.getItemCount()).mapToObj(i -> component.getItemAt(i)).toArray();
	}

	private void assertCheckBoxEq(JCheckBox component) {
		assertThat(component.isSelected()).isEqualTo(choice);
	}

	private void assertChoiceEq(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		if (component instanceof JComboBox) {
			return;
		}
		assertThat(component).isInstanceOf(JCheckBox.class);
		assertThat(choices).isEqualTo(new Object[] { TRUE, FALSE });
	}

}
