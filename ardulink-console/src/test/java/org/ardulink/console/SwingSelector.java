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

import static java.util.stream.IntStream.range;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.JComboBox;

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
public final class SwingSelector {

	private SwingSelector() {
		super();
	}

	public static <T extends Component> T findComponent(Container container, Class<T> type) {
		return findComponent(container, type, __ -> true);
	}

	public static <T extends Component> T findComponent(Container container, Class<T> type,
			Predicate<? super T> predicate) {
		return findComponentRecursively(container, type) //
				.filter(predicate).findFirst().orElse(null);
	}

	private static <T extends Component> Stream<T> findComponentRecursively(Container container, Class<T> clazz) {
		return Arrays.stream(container.getComponents()).flatMap(c -> {
			if (clazz.isInstance(c)) {
				return Stream.of(clazz.cast(c));
			} else if (c instanceof Container) {
				return findComponentRecursively((Container) c, clazz);
			}
			return Stream.empty();
		});
	}

	public static boolean containsItem(JComboBox<?> comboBox, Object item) {
		return Arrays.stream(range(0, comboBox.getItemCount()) //
				.mapToObj(comboBox::getItemAt) //
				.toArray()) //
				.anyMatch(e -> Objects.equals(e, item)) //
		;
	}

}
