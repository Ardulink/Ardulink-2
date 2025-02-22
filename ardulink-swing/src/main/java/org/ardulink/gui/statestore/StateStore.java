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
package org.ardulink.gui.statestore;

import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

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
public class StateStore {

	public static class Decomposer<C extends Component> {

		private final Class<C> componentType;
		private final Function<C, Stream<Component>> decomposer;

		public Decomposer(Class<C> componentType, Function<C, Stream<Component>> decomposer) {
			this.componentType = componentType;
			this.decomposer = decomposer;
		}

		public boolean canHandle(Component component) {
			return componentType.isInstance(component);
		}

		public Stream<Component> decompose(Component component) {
			return decomposer.apply(componentType.cast(component));
		}

	}

	static class Storer<C extends Component, V> {

		private final Class<C> componentType;
		private final Class<V> valueType;
		private final Function<C, V> reader;
		private final BiConsumer<C, V> writer;

		public Storer(Class<C> componentType, Class<V> valueType, Function<C, V> reader, BiConsumer<C, V> writer) {
			this.componentType = componentType;
			this.valueType = valueType;
			this.reader = reader;
			this.writer = writer;
		}

		public boolean canHandle(Component component) {
			return componentType.isInstance(component);
		}

		public void saveTo(Component component, Map<Component, Object> map) {
			map.put(component, reader.apply(componentType.cast(component)));
		}

		public void restoreFrom(Component component, Map<Component, Object> map) {
			Object value = map.get(component);
			if (value != null) {
				writer.accept(componentType.cast(component), valueType.cast(value));
			}
		}

	}

	private static final List<Decomposer<? extends Component>> decomposers = Arrays.asList(
			new Decomposer<>(JTabbedPane.class, t -> range(0, t.getTabCount()).mapToObj(t::getComponentAt)), //
			new Decomposer<>(JPanel.class, p -> stream(p.getComponents())) //
	);

	private static final List<Storer<? extends Component, ? extends Object>> storeHelper = Arrays.asList( //
			new Storer<>(JSlider.class, Integer.class, JSlider::getValue, JSlider::setValue), //
			new Storer<>(JSpinner.class, Object.class, JSpinner::getValue, JSpinner::setValue), //
			new Storer<>(JComboBox.class, Integer.class, JComboBox::getSelectedIndex, JComboBox::setSelectedIndex), //
			new Storer<>(JTextField.class, String.class, JTextField::getText, JTextField::setText), //
			new Storer<>(JCheckBox.class, Boolean.class, JCheckBox::isSelected, JCheckBox::setSelected), //
			new Storer<>(JRadioButton.class, Boolean.class, JRadioButton::isSelected, JRadioButton::setSelected), //
			new Storer<>(JToggleButton.class, Boolean.class, JToggleButton::isSelected, JToggleButton::setSelected) //
	);

	private final Container container;
	private final Map<Component, Object> initialStates = new HashMap<>();

	private Optional<Decomposer<? extends Component>> decomposer(Component component) {
		return decomposers.stream().filter(d -> d.canHandle(component)).findFirst();
	}

	private Optional<Storer<? extends Component, ? extends Object>> storeHelper(Component component) {
		return storeHelper.stream().filter(s -> s.canHandle(component)).findFirst();
	}

	public StateStore(Container container) {
		this.container = container;
	}

	public StateStore snapshot() {
		initialStates.clear();
		storeState(container);
		return this;
	}

	public StateStore restore() {
		restoreState(container);
		return this;
	}

	public StateStore withoutStateOf(Component... components) {
		for (Component component : components) {
			withoutStateOf(component);
		}
		return this;
	}

	public StateStore withoutStateOf(Component component) {
		walk(component, this::withoutStateOf, __ -> initialStates.remove(component));
		return this;
	}

	private void storeState(Component component) {
		walk(component, this::storeState, s -> s.saveTo(component, initialStates));
	}

	private void restoreState(Component component) {
		walk(component, this::restoreState, s -> s.restoreFrom(component, initialStates));
	}

	private void walk(Component component, Consumer<Component> decomposeConsumer,
			Consumer<? super Storer<? extends Component, ? extends Object>> componentConsumer) {
		decomposer(component).ifPresent(d -> d.decompose(component).forEach(decomposeConsumer));
		storeHelper(component).ifPresent(componentConsumer);
	}

}
