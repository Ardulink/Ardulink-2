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
package org.ardulink.gui.util;

import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static org.ardulink.util.Streams.concat;

import java.awt.Component;
import java.awt.Container;
import java.util.stream.Stream;

import javax.swing.JTabbedPane;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class SwingUtilities {

	private SwingUtilities() {
		super();
	}

	public static Stream<Component> componentsStream(Component component) {
		if (component instanceof Container) {
			Stream<Component> children = children(component);
			return component instanceof JTabbedPane //
					? concat(Stream.of(component), children, tabs((JTabbedPane) component)) //
					: concat(Stream.of(component), children);
		}
		return Stream.of(component);
	}

	private static Stream<Component> children(Component component) {
		return Stream.of(((Container) component).getComponents()) //
				.flatMap(SwingUtilities::componentsStream);
	}

	private static Stream<Component> tabs(JTabbedPane tabbedPane) {
		return range(0, tabbedPane.getTabCount()) //
				.mapToObj(tabbedPane::getComponentAt) //
				.flatMap(SwingUtilities::componentsStream);
	}

}
