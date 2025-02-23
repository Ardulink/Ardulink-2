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

import static org.ardulink.gui.util.SwingUtilities.componentsStream;

import java.awt.Component;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 * Store for states of Swing components. Can be used to store the states via
 * {@link #snapshot()} and restores them using {@link #restore()}. Components
 * that should get ignored can be set via {@link #removeStates(Component)} which
 * work recursively.
 */
public class StateStore {

	private static class Storer<C extends Component, V> {

		private final Class<C> componentType;
		private final Class<V> valueType;
		private final Function<C, V> saver;
		private final BiConsumer<C, V> restorer;

		public Storer(Class<C> componentType, Class<V> valueType, Function<C, V> saver, BiConsumer<C, V> restorer) {
			this.componentType = componentType;
			this.valueType = valueType;
			this.saver = saver;
			this.restorer = restorer;
		}

		public boolean canHandle(Component component) {
			return componentType.isInstance(component);
		}

		public void save(Component component, Map<Component, SoftReference<Object>> map) {
			map.put(component, new SoftReference<>(saver.apply(componentType.cast(component))));
		}

		public void restore(Component component, Map<Component, SoftReference<Object>> map) {
			SoftReference<Object> ref = map.get(component);
			if (ref != null) {
				Object value = ref.get();
				if (value != null) {
					restorer.accept(componentType.cast(component), valueType.cast(value));
				}
			}
		}

		@Override
		public String toString() {
			return "Storer [componentType=" + componentType + ", valueType=" + valueType + ", saver=" + saver
					+ ", restorer=" + restorer + "]";
		}

	}

	private static final List<Storer<? extends Component, ? extends Object>> storers = Arrays.asList( //
			new Storer<>(JLabel.class, String.class, JLabel::getText, JLabel::setText), //
			new Storer<>(JTextField.class, String.class, JTextField::getText, JTextField::setText), //
			new Storer<>(JCheckBox.class, Boolean.class, JCheckBox::isSelected, JCheckBox::setSelected), //
			new Storer<>(JToggleButton.class, Boolean.class, JToggleButton::isSelected, JToggleButton::setSelected), //
			new Storer<>(JRadioButton.class, Boolean.class, JRadioButton::isSelected, JRadioButton::setSelected), //
			new Storer<>(JComboBox.class, Integer.class, JComboBox::getSelectedIndex, JComboBox::setSelectedIndex), //
			new Storer<>(JSlider.class, Integer.class, JSlider::getValue, JSlider::setValue), //
			new Storer<>(JSpinner.class, Object.class, JSpinner::getValue, JSpinner::setValue), //
			new Storer<>(JProgressBar.class, Integer.class, JProgressBar::getValue, JProgressBar::setValue) //
	);

	private final Component component;
	private final Map<Component, SoftReference<Object>> states = new IdentityHashMap<>();

	private static Optional<Storer<? extends Component, ? extends Object>> storer(Component component) {
		return storers.stream().filter(s -> s.canHandle(component)).findFirst();
	}

	public StateStore(Component component) {
		this.component = component;
	}

	public StateStore removeStates(Component... components) {
		Stream.of(components).forEach(this::removeStates);
		return this;
	}

	public StateStore removeStates(Component component) {
		return forAllComponents(component, (__, c) -> states.remove(c));
	}

	public StateStore snapshot() {
		states.clear();
		return forAllComponents(component, (s, c) -> s.save(c, states));
	}

	public StateStore restore() {
		return forAllComponents(component, (s, c) -> s.restore(c, states));
	}

	private StateStore forAllComponents(Component component, BiConsumer<Storer<?, ?>, Component> consumer) {
		componentsStream(component).forEach(c -> storer(c).ifPresent(s -> consumer.accept(s, c)));
		return this;
	}

	@Override
	public String toString() {
		return "StateStore [component=" + component + ", states=" + states + "]";
	}

}
