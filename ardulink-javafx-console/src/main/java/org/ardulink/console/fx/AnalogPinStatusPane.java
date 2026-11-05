package org.ardulink.console.fx;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.events.FilteredEventListenerAdapter.filter;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.util.Throwables;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AnalogPinStatusPane extends VBox implements Linkable {

	private static final float VOLTAGE_CONVERSION = 5f / 1023f;
	private final DecimalFormat voltageFormat = new DecimalFormat("#.### V");

	private final ComboBox<Integer> pinComboBox;
	private final ComboBox<Integer> maxComboBox;
	private final ComboBox<Integer> minComboBox;
	private final ProgressBar progressBar;
	private final Label valueLabel;
	private final Label voltLabel;
	private final ToggleButton activeButton;
	private Link link = NullLink.NULL_LINK;
	private EventListener listener;

	public AnalogPinStatusPane() {
		super(4);
		setPadding(new Insets(5));
		setAlignment(Pos.CENTER);

		Label title = new Label("Analog PIN Status");
		title.setStyle("-fx-font-weight: bold;");

		pinComboBox = new ComboBox<>(FXCollections.observableArrayList(
				IntStream.rangeClosed(0, 40).boxed().collect(Collectors.toList())));
		pinComboBox.getSelectionModel().selectFirst();

		maxComboBox = new ComboBox<>(FXCollections.observableArrayList(
				IntStream.rangeClosed(0, 1023).boxed().collect(Collectors.toList())));
		maxComboBox.getSelectionModel().select(Integer.valueOf(1023));

		minComboBox = new ComboBox<>(FXCollections.observableArrayList(
				IntStream.rangeClosed(0, 1023).boxed().collect(Collectors.toList())));
		minComboBox.getSelectionModel().selectFirst();

		progressBar = new ProgressBar(0);
		progressBar.setPrefWidth(140);

		valueLabel = new Label("0");
		voltLabel = new Label("0.000 V");

		activeButton = new ToggleButton("Off");
		activeButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
			if (isSelected) {
				startListening();
			} else {
				stopListening();
			}
			updateSubControlState();
		});

		maxComboBox.valueProperty().addListener((obs, old, val) -> fixRange());
		minComboBox.valueProperty().addListener((obs, old, val) -> fixRange());

		getChildren().addAll(
				title,
				labeledRow("Pin:", pinComboBox),
				labeledRow("Max:", maxComboBox),
				progressBar,
				labeledRow("Value:", valueLabel),
				labeledRow("Voltage:", voltLabel),
				activeButton,
				labeledRow("Min:", minComboBox));

		updateDisplay(0);
	}

	private static HBox labeledRow(String text, javafx.scene.Node... nodes) {
		HBox row = new HBox(5);
		row.setAlignment(Pos.CENTER_LEFT);
		row.getChildren().add(new Label(text));
		row.getChildren().addAll(nodes);
		return row;
	}

	private void fixRange() {
		int min = minComboBox.getValue() == null ? 0 : minComboBox.getValue();
		int max = maxComboBox.getValue() == null ? 1023 : maxComboBox.getValue();
		if (min > max) {
			maxComboBox.getSelectionModel().select(Integer.valueOf(min));
		}
	}

	private void updateDisplay(int value) {
		int min = minComboBox.getValue() == null ? 0 : minComboBox.getValue();
		int max = maxComboBox.getValue() == null ? 1023 : maxComboBox.getValue();
		int clamped = Math.max(min, Math.min(max, value));
		valueLabel.setText(String.valueOf(clamped));
		voltLabel.setText(voltageFormat.format(clamped * VOLTAGE_CONVERSION));
		progressBar.setProgress(max == min ? 0 : (double) (clamped - min) / (max - min));
	}

	private void startListening() {
		try {
			listener = filter(analogPin(pinComboBox.getValue()), new EventListenerAdapter() {
				@Override
				public void stateChanged(AnalogPinValueChangedEvent event) {
					Platform.runLater(() -> updateDisplay(event.getValue().intValue()));
				}
			});
			link.addListener(listener);
			activeButton.setText("On");
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private void stopListening() {
		if (listener != null) {
			try {
				link.removeListener(listener);
			} catch (IOException e) {
				throw Throwables.propagate(e);
			} finally {
				listener = null;
				activeButton.setText("Off");
			}
		}
	}

	private void updateSubControlState() {
		boolean isOn = activeButton.isSelected();
		pinComboBox.setDisable(isOn);
		minComboBox.setDisable(isOn);
		maxComboBox.setDisable(isOn);
		progressBar.setDisable(!isOn);
	}

	public AnalogPinStatusPane setPin(int pin) {
		pinComboBox.getSelectionModel().select(Integer.valueOf(pin));
		return this;
	}

	@Override
	public void setLink(Link link) {
		stopListening();
		this.link = link;
	}
}
