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
import static java.util.stream.IntStream.range;
import static org.ardulink.gui.util.SwingUtilities.componentsStream;

import java.awt.Component;
import java.awt.Container;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.JComboBox;

/**
 * [ardulinktitle] [ardulinkversion]
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
		return findComponentRecursively(container, type) //
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(format("No match in %s for type %s", container, type)));
	}

	public static <T extends Component> T findComponent(Container container, Class<T> type,
			Predicate<? super T> predicate) {
		return tryFindComponent(container, type, predicate) //
				.orElseThrow(() -> new IllegalStateException(
						format("No match in %s for type %s and predicate %s", container, type, predicate)));
	}

	public static <T extends Component> Optional<T> tryFindComponent(Container container, Class<T> type,
			Predicate<? super T> predicate) {
		return findComponentRecursively(container, type).filter(predicate).findFirst();
	}

	private static <T extends Component> Stream<T> findComponentRecursively(Container container, Class<T> clazz) {
		return componentsStream(container).filter(clazz::isInstance).map(clazz::cast);
	}

	public static boolean containsItem(JComboBox<?> comboBox, Object item) {
		return range(0, comboBox.getItemCount()) //
				.mapToObj(comboBox::getItemAt) //
				.anyMatch(isEqual(item));
	}

}
