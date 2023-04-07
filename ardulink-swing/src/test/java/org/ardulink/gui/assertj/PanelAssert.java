package org.ardulink.gui.assertj;

import static java.util.stream.IntStream.range;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.TestFactory;

public class PanelAssert extends AbstractAssert<PanelAssert, Component> {

	public static class RowAssert {

		private final List<Component> rowComponents;

		private RowAssert(List<Component> rowComponents) {
			this.rowComponents = rowComponents;
		}

		public RowAssert labeled(String label) {
			Assertions.assertThat(label()).isInstanceOfSatisfying(JLabel.class,
					l -> Assertions.assertThat(l.getText()).isEqualTo(label));
			return this;
		}

		public RowAssert havingValue(String value) {
			Assertions.assertThat(component()).isInstanceOfSatisfying(JTextField.class,
					t -> Assertions.assertThat(t.getText()).isEqualTo(value));
			return this;
		}

		public RowAssert havingValue(int value) {
			Assertions.assertThat(component()).isInstanceOfSatisfying(JSpinner.class,
					s -> Assertions.assertThat(s.getValue()).isEqualTo(value));
			return this;
		}

		public <T> RowAssert havingChoice(Object[] choices, Object choice) {
			Assertions.assertThat(component()).isInstanceOfSatisfying(JComboBox.class, c -> {
				Assertions.assertThat(items(c)).containsExactly(choices);
				Assertions.assertThat(c.getSelectedItem()).isEqualTo(choice);
			});
			return this;
		}

		public RowAssert havingYesNoValue(Object choice) {
			Assertions.assertThat(component()).isInstanceOfSatisfying(JCheckBox.class,
					c -> Assertions.assertThat(c.isSelected()).isEqualTo(choice));
			return this;
		}

		private Component label() {
			return rowComponents.get(0);
		}

		private Component component() {
			return rowComponents.get(1);
		}

		private static Object[] items(JComboBox<?> component) {
			return range(0, component.getItemCount()).mapToObj(component::getItemAt).toArray();
		}

	}

	private static final int COMPONENTS_PER_ROW = 3;

	@TestFactory
	public static PanelAssert assertThat(JPanel panel) {
		return new PanelAssert(panel);
	}

	private final JPanel panel;

	private PanelAssert(JPanel panel) {
		super(panel, PanelAssert.class);
		this.panel = panel;
	}

	public RowAssert hasRow(int index) {
		int from = index * COMPONENTS_PER_ROW;
		List<Component> components = Arrays.asList(panel.getComponents());
		int rowCount = components.size() / COMPONENTS_PER_ROW;
		Assertions.assertThat(index).isLessThan(rowCount)
				.withFailMessage(() -> components + " has " + rowCount + " rows");
		return new RowAssert(components.subList(from, from + COMPONENTS_PER_ROW - 1));
	}

}