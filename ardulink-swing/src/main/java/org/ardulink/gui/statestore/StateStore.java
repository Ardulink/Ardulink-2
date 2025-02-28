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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.ardulink.gui.statestore.StateStore.Storer.storer;
import static org.ardulink.gui.util.SwingUtilities.componentsStream;

import java.awt.Component;
import java.awt.IllegalComponentStateException;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.ardulink.gui.util.SwingUtilities;
import org.ardulink.util.Primitives;
import org.ardulink.util.Throwables;

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

	private static final String TEXT = "text";
	private static final String ICON = "icon";
	private static final String SELECT = "selected";
	private static final String SELECTED_ITEM = "selectedItem";
	private static final String VALUE = "value";

	@Retention(RUNTIME)
	@Target(FIELD)
	public static @interface Restorable {

	}

	public static class Storer<C extends Component, V> {

		private final Class<C> componentType;
		private final String valueName;
		private final Class<V> valueType;
		private final Function<C, V> saver;
		private final BiConsumer<C, V> restorer;

		public Storer(Class<C> componentType, String valueName, Class<V> valueType, Function<C, V> saver,
				BiConsumer<C, V> restorer) {
			this.componentType = componentType;
			this.valueName = valueName;
			this.valueType = valueType;
			this.saver = saver;
			this.restorer = restorer;
		}

		public boolean canHandle(Component component) {
			return componentType.isInstance(component);
		}

		public void save(Component component, Map<Component, SoftReference<Map<String, Object>>> states) {
			SoftReference<Map<String, Object>> ref = states.computeIfAbsent(component,
					__ -> new SoftReference<Map<String, Object>>(new HashMap<String, Object>()));
			Map<String, Object> map = ref.get();
			if (map != null) {
				map.put(valueName, saver.apply(componentType.cast(component)));
			}
		}

		public void restore(Component component, Map<Component, SoftReference<Map<String, Object>>> states) {
			SoftReference<Map<String, Object>> ref = states.get(component);
			if (ref != null) {
				Map<String, Object> map = ref.get();
				if (map != null) {
					restorer.accept(componentType.cast(component), valueType.cast(map.get(valueName)));
				}
			}
		}

		@Override
		public String toString() {
			return "Storer [componentType=" + componentType + ", valueType=" + valueType + ", saver=" + saver
					+ ", restorer=" + restorer + "]";
		}

		@SuppressWarnings("unchecked")
		public static <C extends Component, V> Storer<C, V> storer(Class<C> componentType, String attribute) {
			try {
				PropertyDescriptor propertyDescriptor = Stream
						.of(Introspector.getBeanInfo(componentType).getPropertyDescriptors())
						.filter(pd -> pd.getName().equals(attribute)).findFirst()
						.orElseThrow(() -> new IllegalComponentStateException(
								componentType.getName() + " does not define attribute named " + attribute));
				Class<V> propertyType = (Class<V>) Primitives.wrap(propertyDescriptor.getPropertyType());
				Method readMethod = propertyDescriptor.getReadMethod();
				Method writeMethod = propertyDescriptor.getWriteMethod();
				return new Storer<>(componentType, propertyDescriptor.getName(), propertyType, c -> {
					try {
						return (V) readMethod.invoke(c);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw Throwables.propagate(e);
					}
				}, (c, v) -> {
					try {
						writeMethod.invoke(c, v);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw Throwables.propagate(e);
					}
				});
			} catch (IntrospectionException e) {
				throw Throwables.propagate(e);
			}
		}

	}

	private static final List<Storer<? extends Component, ? extends Object>> storers = Arrays.asList( //
			storer(JLabel.class, TEXT), //
			storer(JLabel.class, ICON), //
			storer(JTextField.class, TEXT), //
			storer(JCheckBox.class, SELECT), //
			storer(JToggleButton.class, SELECT), //
			storer(JRadioButton.class, SELECT), //
			storer(JComboBox.class, SELECTED_ITEM), //
			storer(JSlider.class, VALUE), //
			storer(JSpinner.class, VALUE), //
			storer(JProgressBar.class, VALUE) //
	);

	private final Function<Component, Stream<Component>> componentStreamer;
	private final Component component;
	private final Map<Component, SoftReference<Map<String, Object>>> states = new IdentityHashMap<>();

	private static Stream<Storer<? extends Component, ? extends Object>> storers(Component component) {
		return storers.stream().filter(s -> s.canHandle(component));
	}

	public static Function<Component, Stream<Component>> restorables() {
		return c -> componentsStream(c).filter(JPanel.class::isInstance).map(JPanel.class::cast)
				.flatMap(StateStore::restorables);
	}

	private static Stream<JComponent> restorables(JPanel panel) {
		return Stream.of(panel.getClass().getDeclaredFields()) //
				.filter(f -> f.isAnnotationPresent(Restorable.class)) //
				.filter(f -> JComponent.class.isAssignableFrom(f.getType())) //
				.map(f -> {
					f.setAccessible(true);
					try {
						return (JComponent) f.get(panel);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw Throwables.propagate(e);
					}
				});
	}

	public StateStore(Component component) {
		this(component, SwingUtilities::componentsStream);
	}

	public StateStore(Component component, Function<Component, Stream<Component>> componentStreamer) {
		this.component = component;
		this.componentStreamer = componentStreamer;
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
		componentStreamer.apply(component).forEach(c -> storers(c).forEach(s -> consumer.accept(s, c)));
		return this;
	}

	@Override
	public String toString() {
		return "StateStore [component=" + component + ", states=" + states + "]";
	}

}
