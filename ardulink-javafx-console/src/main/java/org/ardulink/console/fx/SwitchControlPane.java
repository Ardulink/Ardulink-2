package org.ardulink.console.fx;

import static org.ardulink.core.Pin.digitalPin;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.util.Throwables;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SwitchControlPane extends VBox implements Linkable {

	private final ComboBox<Integer> pinComboBox;
	private final ToggleButton switchButton;
	private Link link = NullLink.NULL_LINK;

	public SwitchControlPane() {
		super(5);
		setPadding(new Insets(5));
		setAlignment(Pos.CENTER);

		pinComboBox = new ComboBox<>(FXCollections.observableArrayList(
				IntStream.rangeClosed(0, 40).boxed().collect(Collectors.toList())));
		pinComboBox.getSelectionModel().select(Integer.valueOf(3));

		HBox pinRow = new HBox(5, new Label("Digital Pin:"), pinComboBox);
		pinRow.setAlignment(Pos.CENTER_LEFT);

		switchButton = new ToggleButton("Off");
		switchButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
			try {
				link.switchDigitalPin(digitalPin(pinComboBox.getValue()), isSelected);
				switchButton.setText(isSelected ? "On" : "Off");
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		});

		getChildren().addAll(pinRow, switchButton);
	}

	public SwitchControlPane setPin(int pin) {
		pinComboBox.getSelectionModel().select(Integer.valueOf(pin));
		return this;
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}
}
