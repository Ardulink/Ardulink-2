package org.ardulink.console.fx;

import static org.ardulink.core.Pin.analogPin;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.util.Throwables;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class PWMControlPane extends VBox implements Linkable {

	public interface PWMControllerListener {
		void pwmChanged(PWMChangeEvent event);
	}

	public static class PWMChangeEvent {
		private final int pwmValue;

		public PWMChangeEvent(int pwmValue) {
			this.pwmValue = pwmValue;
		}

		public int pwmValue() {
			return pwmValue;
		}
	}

	private static final float VOLTAGE_CONVERSION = 5f / 255f;
	private final DecimalFormat voltageFormat = new DecimalFormat("#.### V");

	private final ComboBox<Integer> pinComboBox;
	private final ComboBox<Integer> maxComboBox;
	private final ComboBox<Integer> minComboBox;
	private final Slider slider;
	private final ProgressBar progressBar;
	private final Label voltLabel;
	private final Label currentValueLabel;
	private final Label titleLabel;
	private final CheckBox contChangeBox;
	private final List<PWMControllerListener> listeners = new ArrayList<>();

	private Link link = NullLink.NULL_LINK;
	private ChangeListener<Number> sliderListener;

	public PWMControlPane() {
		super(4);
		setPadding(new Insets(5));
		setAlignment(Pos.CENTER);

		titleLabel = new Label("Power Pin Controller");
		titleLabel.setStyle("-fx-font-weight: bold;");

		pinComboBox = intComboBox(0, 40, 11);
		maxComboBox = intComboBox(0, 1023, 255);
		minComboBox = intComboBox(0, 1023, 0);
		currentValueLabel = new Label("0");
		voltLabel = new Label("0.000 V");

		slider = new Slider(0, 255, 0);
		slider.setOrientation(Orientation.VERTICAL);
		slider.setPrefHeight(130);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(64);

		// The bar is 130 px long × 16 px thick; rotating 270° makes it
		// visually 16 px wide × 130 px tall (bottom-to-top fill = low-to-high power).
		// Without the Pane wrapper the HBox would allocate 130 px of horizontal
		// layout space (the unrotated width) instead of the correct 16 px.
		progressBar = new ProgressBar(0);
		progressBar.setPrefWidth(130);
		progressBar.setPrefHeight(16);
		progressBar.setRotate(270);
		// Centre the 130×16 bar inside the 16×130 wrapper pane.
		progressBar.setLayoutX(-(130 - 16) / 2.0);
		progressBar.setLayoutY((130 - 16) / 2.0);

		Pane progressBarPane = new Pane(progressBar);
		progressBarPane.setPrefSize(16, 130);
		progressBarPane.setMaxSize(16, 130);
		progressBarPane.setMinSize(16, 130);

		contChangeBox = new CheckBox("Continuous Change");
		contChangeBox.setSelected(true);

		sliderListener = (obs, oldVal, newVal) -> {
			if (!slider.isValueChanging() || contChangeBox.isSelected()) {
				onSliderValue(newVal.intValue());
			}
		};
		slider.valueProperty().addListener(sliderListener);

		maxComboBox.valueProperty().addListener((obs, old, val) -> {
			if (val != null) {
				slider.setMax(val);
				slider.setMajorTickUnit(Math.max(1, (val - (int) slider.getMin()) / 4));
			}
		});
		minComboBox.valueProperty().addListener((obs, old, val) -> {
			if (val != null) {
				slider.setMin(val);
			}
		});

		HBox sliderRow = new HBox(8, progressBarPane, slider);
		sliderRow.setAlignment(Pos.CENTER);

		getChildren().addAll(
				titleLabel,
				labeledRow("Pin:", pinComboBox),
				labeledRow("Max Value:", maxComboBox),
				sliderRow,
				labeledRow("Current Value:", currentValueLabel),
				labeledRow("Volt Output:", voltLabel),
				contChangeBox,
				labeledRow("Min Value:", minComboBox));

		updateDisplay(0);
	}

	private void onSliderValue(int power) {
		updateDisplay(power);
		try {
			link.switchAnalogPin(analogPin(pinComboBox.getValue()), power);
			notifyListeners(new PWMChangeEvent(power));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private void updateDisplay(int power) {
		currentValueLabel.setText(String.valueOf(power));
		voltLabel.setText(voltageFormat.format(power * VOLTAGE_CONVERSION));
		int min = (int) slider.getMin();
		int max = (int) slider.getMax();
		progressBar.setProgress(max == min ? 0 : (double) (power - min) / (max - min));
	}

	private static HBox labeledRow(String labelText, javafx.scene.Node... nodes) {
		HBox row = new HBox(5);
		row.setAlignment(Pos.CENTER_LEFT);
		row.getChildren().add(new Label(labelText));
		row.getChildren().addAll(nodes);
		return row;
	}

	private static ComboBox<Integer> intComboBox(int from, int to, int defaultValue) {
		ComboBox<Integer> cb = new ComboBox<>(FXCollections.observableArrayList(
				IntStream.rangeClosed(from, to).boxed().collect(Collectors.toList())));
		cb.getSelectionModel().select(Integer.valueOf(defaultValue));
		return cb;
	}

	public PWMControlPane setPin(int pin) {
		pinComboBox.getSelectionModel().select(Integer.valueOf(pin));
		return this;
	}

	public void setTitle(String title) {
		titleLabel.setText(title);
	}

	public int getValue() {
		return (int) slider.getValue();
	}

	public void setValue(int value) {
		slider.valueProperty().removeListener(sliderListener);
		slider.setValue(Math.max(slider.getMin(), Math.min(slider.getMax(), value)));
		updateDisplay((int) slider.getValue());
		slider.valueProperty().addListener(sliderListener);
	}

	public boolean addPWMControllerListener(PWMControllerListener listener) {
		return listeners.add(listener);
	}

	public boolean removePWMControllerListener(PWMControllerListener listener) {
		return listeners.remove(listener);
	}

	private void notifyListeners(PWMChangeEvent event) {
		listeners.forEach(l -> l.pwmChanged(event));
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}
}
