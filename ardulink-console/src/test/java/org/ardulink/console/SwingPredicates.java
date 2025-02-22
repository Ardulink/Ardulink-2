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
package org.ardulink.console;

import static java.lang.String.format;
import static java.util.function.Predicate.isEqual;
import static org.ardulink.util.Predicates.attribute;

import java.util.function.Predicate;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class SwingPredicates {

	private SwingPredicates() {
		super();
	}

	public static Predicate<JComponent> withName(String name) {
		return attribute(JComponent::getName, isEqual(name));
	}

	public static Predicate<AbstractButton> buttonWithText(String text) {
		return attribute(AbstractButton::getText, isEqual(text));
	}

	public static Predicate<JLabel> labelWithText(String text) {
		return attribute(JLabel::getText, isEqual(text));
	}

	@SuppressWarnings("rawtypes")
	public static Predicate<JComboBox> withSelectedItem(Object item) {
		return attribute(JComboBox::getSelectedItem, isEqual(item));
	}

	public static <T> Predicate<Object> isA(Class<T> type, Predicate<? super T> predicate) {
		return new Predicate<Object>() {

			@Override
			public boolean test(Object component) {
				return type.isInstance(component) && predicate.test(type.cast(component));
			}

			@Override
			public String toString() {
				return format("is type: %s, predicate: %s", type, predicate);
			}

		};
	}

}
