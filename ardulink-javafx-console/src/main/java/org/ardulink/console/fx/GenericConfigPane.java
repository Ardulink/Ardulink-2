package org.ardulink.console.fx;

import java.util.Arrays;
import java.util.List;

import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

public class GenericConfigPane extends GridPane {

	public GenericConfigPane(Configurer configurer) {
		setHgap(8);
		setVgap(6);
		setPadding(new Insets(8));

		int row = 0;
		for (String name : configurer.getAttributes()) {
			ConfigAttribute attribute = configurer.getAttribute(name);
			Label label = new Label(attribute.getName());
			if (attribute.getDescription() != null) {
				label.setTooltip(new Tooltip(attribute.getDescription()));
			}
			add(label, 0, row);

			boolean hasDiscoverButton = attribute.choiceDependsOn().length > 0;
			javafx.scene.Node control = createControl(attribute);
			if (attribute.getDescription() != null && control instanceof javafx.scene.control.Control) {
				((javafx.scene.control.Control) control).setTooltip(new Tooltip(attribute.getDescription()));
			}
			GridPane.setHgrow(control, Priority.ALWAYS);
			add(control, 1, row);

			if (hasDiscoverButton && control instanceof ComboBox) {
				@SuppressWarnings("unchecked")
				ComboBox<Object> comboBox = (ComboBox<Object>) control;
				Button discoverBtn = new Button("...");
				discoverBtn.setTooltip(new Tooltip("Discover"));
				discoverBtn.setOnAction(__ -> comboBox.setItems(
						FXCollections.observableArrayList(attribute.getChoiceValues())));
				add(discoverBtn, 2, row);
			}
			row++;
		}
	}

	private static javafx.scene.Node createControl(ConfigAttribute attribute) {
		Class<?> type = attribute.getType();
		if (isBoolean(type)) {
			return createCheckBox(attribute);
		}
		if (attribute.hasChoiceValues()) {
			return createComboBox(attribute);
		}
		if (isNumber(type)) {
			return createSpinner(attribute);
		}
		return createTextField(attribute);
	}

	private static CheckBox createCheckBox(ConfigAttribute attribute) {
		CheckBox cb = new CheckBox();
		Object val = attribute.getValue();
		cb.setSelected(val instanceof Boolean && (Boolean) val);
		cb.selectedProperty().addListener((obs, old, newVal) -> attribute.setValue(newVal));
		return cb;
	}

	@SuppressWarnings("unchecked")
	private static ComboBox<Object> createComboBox(ConfigAttribute attribute) {
		ComboBox<Object> cb = new ComboBox<>(FXCollections.observableArrayList());

		if (attribute.getType().isEnum()) {
			// Enum choices are constant; populate immediately
			Object[] enumChoices = attribute.getChoiceValues();
			if (enumChoices != null) {
				cb.getItems().setAll(enumChoices);
			}
			StringConverter<Object> converter = new StringConverter<>() {
				@Override
				public String toString(Object value) {
					if (value == null) {
						return "";
					}
					String desc = attribute.getChoiceDescription(value);
					return desc != null ? desc : String.valueOf(value);
				}

				@Override
				public Object fromString(String s) {
					return null;
				}
			};
			cb.setConverter(converter);
			cb.setCellFactory(__ -> new ListCell<>() {
				@Override
				protected void updateItem(Object item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty || item == null ? "" : converter.toString(item));
				}
			});
		} else {
			// Non-enum choices may require I/O (e.g. serial port scan); load lazily
			cb.setOnShowing(__ -> {
				Object[] choices = attribute.getChoiceValues();
				if (choices != null) {
					cb.getItems().setAll(choices);
				}
			});
		}

		Object currentValue = attribute.getValue();
		if (currentValue != null) {
			cb.getSelectionModel().select(currentValue);
		} else if (!cb.getItems().isEmpty()) {
			cb.getSelectionModel().selectFirst();
		}

		cb.valueProperty().addListener((obs, old, newVal) -> attribute.setValue(newVal));
		return cb;
	}

	private static javafx.scene.control.Control createSpinner(ConfigAttribute attribute) {
		Object val = attribute.getValue();
		Class<?> type = attribute.getType();
		if (type == Integer.class || type == int.class) {
			return newSpinner(attribute, new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE,
					num(val).intValue()));
		}
		if (type == Long.class || type == long.class) {
			long initial = num(val).longValue();
			return newSpinner(attribute, new SpinnerValueFactory<Number>() {
				{
					setValue(initial);
				}

				@Override
				public void decrement(int steps) {
					setValue(((Number) getValue()).longValue() - steps);
				}

				@Override
				public void increment(int steps) {
					setValue(((Number) getValue()).longValue() + steps);
				}
			});
		}
		double initial = num(val).doubleValue();
		return newSpinner(attribute, new SpinnerValueFactory<Number>() {
			{
				setValue(initial);
			}

			@Override
			public void decrement(int steps) {
				setValue(((Number) getValue()).doubleValue() - steps);
			}

			@Override
			public void increment(int steps) {
				setValue(((Number) getValue()).doubleValue() + steps);
			}
		});
	}

	private static Number num(Object val) {
		return val instanceof Number ? (Number) val : 0;
	}

	private static <T extends Number> javafx.scene.control.Control newSpinner(ConfigAttribute attribute,
			SpinnerValueFactory<T> factory) {
		Spinner<T> spinner = new Spinner<>(factory);
		spinner.setEditable(true);
		spinner.valueProperty().addListener((obs, old, newVal) -> attribute.setValue(newVal));
		return spinner;
	}

	private static TextField createTextField(ConfigAttribute attribute) {
		Object val = attribute.getValue();
		TextField tf = new TextField(val == null ? "" : String.valueOf(val));
		tf.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
			if (!isFocused) {
				attribute.setValue(tf.getText());
			}
		});
		return tf;
	}

	private static boolean isBoolean(Class<?> type) {
		return type == Boolean.class || type == boolean.class;
	}

	private static boolean isNumber(Class<?> type) {
		return Number.class.isAssignableFrom(type)
				|| type == int.class || type == long.class
				|| type == float.class || type == double.class;
	}
}
