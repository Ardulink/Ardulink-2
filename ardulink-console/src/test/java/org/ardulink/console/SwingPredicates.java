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

import static org.ardulink.util.Predicates.attribute;

import java.util.function.Predicate;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * [ardulinktitle] [ardulinkversion] This is the ready ardulink console a
 * complete SWING application to manage an Arduino board. Console has several
 * tabs with all ready arduino components. Each tab is able to do a specific
 * action sending or listening for messages to arduino or from arduino board.
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
		return attribute(JComponent::getName, name::equals);
	}

	public static Predicate<AbstractButton> buttonWithText(String text) {
		return attribute(AbstractButton::getText, text::equals);
	}

	public static Predicate<JLabel> labelWithText(String text) {
		return attribute(JLabel::getText, text::equals);
	}

	@SuppressWarnings("rawtypes")
	public static Predicate<JComboBox> withSelectedItem(Object item) {
		return attribute(JComboBox::getSelectedItem, item::equals);
	}

	public static <T> Predicate<JComponent> is(Class<T> type, Predicate<? super T> predicate) {
		return c -> type.isInstance(c) && predicate.test(type.cast(c));
	}

}
