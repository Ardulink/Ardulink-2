package org.ardulink.console;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class StateStore {

	private static class Storer<C extends Component, V> {

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

		public Class<C> getComponentType() {
			return componentType;
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

	private Stream<Storer<? extends Component, ? extends Object>> storeHelper(Component component) {
		return storeHelper.stream().filter(s -> s.getComponentType().isInstance(component));
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

	public StateStore removeState(Component component) {
		walk(component, this::removeState, __ -> initialStates.remove(component));
		return this;
	}

	private void storeState(Component component) {
		walk(component, this::storeState, s -> s.saveTo(component, initialStates));
	}

	private void restoreState(Component component) {
		walk(component, this::restoreState, s -> s.restoreFrom(component, initialStates));
	}

	private void walk(Component component, Consumer<Component> flatmapper,
			Consumer<? super Storer<? extends Component, ? extends Object>> consumer) {
		if (component instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) component;
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				flatmapper.accept(tabbedPane.getComponentAt(i));
			}
		} else if (component instanceof JPanel) {
			for (Component child : ((JPanel) component).getComponents()) {
				flatmapper.accept(child);
			}
		} else {
			storeHelper(component).findFirst().ifPresent(consumer);
		}
	}

}
