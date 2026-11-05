package org.ardulink.console.fx;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.Pin.analogPin;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.core.Tone;
import org.ardulink.util.Throwables;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TonePane extends VBox implements Linkable {

	private final ComboBox<Integer> pinComboBox;
	private final Spinner<Integer> frequencySpinner;
	private final Spinner<Integer> durationSpinner;
	private final CheckBox durationEnabledBox;
	private final ToggleButton toneButton;
	private Link link = NullLink.NULL_LINK;

	public TonePane() {
		super(6);
		setPadding(new Insets(10));
		setAlignment(Pos.CENTER_LEFT);

		pinComboBox = new ComboBox<>(FXCollections.observableArrayList(
				IntStream.rangeClosed(0, 40).boxed().collect(Collectors.toList())));
		pinComboBox.getSelectionModel().selectFirst();
		HBox pinRow = new HBox(5, new Label("Pin:"), pinComboBox);
		pinRow.setAlignment(Pos.CENTER_LEFT);

		frequencySpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 31));
		frequencySpinner.setEditable(true);
		frequencySpinner.setPrefWidth(100);
		HBox freqRow = new HBox(5, new Label("Frequency (Hz):"), frequencySpinner);
		freqRow.setAlignment(Pos.CENTER_LEFT);

		durationSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1000));
		durationSpinner.setEditable(true);
		durationSpinner.setPrefWidth(100);

		durationEnabledBox = new CheckBox("Duration enabled");
		durationEnabledBox.setSelected(true);
		durationEnabledBox.selectedProperty().addListener((obs, old, val) -> {
			durationSpinner.setDisable(!val);
		});

		HBox durationRow = new HBox(5, new Label("Duration (ms):"), durationSpinner, durationEnabledBox);
		durationRow.setAlignment(Pos.CENTER_LEFT);

		toneButton = new ToggleButton("Off");
		toneButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
			Integer pin = pinComboBox.getValue();
			if (pin == null) {
				return;
			}
			try {
				if (isSelected) {
					Tone.Builder tone = Tone.forPin(analogPin(pin)).withHertz(frequencySpinner.getValue());
					link.sendTone(durationEnabledBox.isSelected()
							? tone.withDuration(durationSpinner.getValue(), MILLISECONDS)
							: tone.endless());
				} else {
					link.sendNoTone(analogPin(pin));
				}
				toneButton.setText(isSelected ? "On" : "Off");
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		});

		getChildren().addAll(pinRow, freqRow, durationRow, toneButton);
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}
}
