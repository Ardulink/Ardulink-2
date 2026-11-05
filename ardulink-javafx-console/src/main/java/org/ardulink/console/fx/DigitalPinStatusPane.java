package org.ardulink.console.fx;

import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.FilteredEventListenerAdapter.filter;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.util.Throwables;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DigitalPinStatusPane extends VBox implements Linkable {

	private static final Image HIGH_ICON = loadIcon("icons/blue-on-32.png");
	private static final Image LOW_ICON = loadIcon("icons/blue-off-32.png");

	private final ComboBox<Integer> pinComboBox;
	private final Label stateLabel;
	private final ImageView stateIcon;
	private final ToggleButton activeButton;
	private Link link = NullLink.NULL_LINK;
	private EventListener listener;

	public DigitalPinStatusPane() {
		super(5);
		setPadding(new Insets(5));
		setAlignment(Pos.CENTER);

		stateIcon = new ImageView(LOW_ICON);
		stateIcon.setFitWidth(32);
		stateIcon.setFitHeight(32);
		stateLabel = new Label("Low");
		HBox stateRow = new HBox(5, stateIcon, stateLabel);
		stateRow.setAlignment(Pos.CENTER);

		pinComboBox = new ComboBox<>(FXCollections.observableArrayList(
				IntStream.rangeClosed(2, 40).boxed().collect(Collectors.toList())));
		pinComboBox.getSelectionModel().selectFirst();

		HBox pinRow = new HBox(5, new Label("Pin:"), pinComboBox);
		pinRow.setAlignment(Pos.CENTER_LEFT);

		activeButton = new ToggleButton("Sensor off");
		activeButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
			if (isSelected) {
				startListening();
			} else {
				stopListening();
			}
			updateSubControlState();
		});

		getChildren().addAll(stateRow, pinRow, activeButton);
		setValue(false);
	}

	private static Image loadIcon(String path) {
		return new Image(DigitalPinStatusPane.class.getResourceAsStream(path));
	}

	private void setValue(boolean value) {
		Platform.runLater(() -> {
			stateLabel.setText(value ? "High" : "Low");
			stateIcon.setImage(value ? HIGH_ICON : LOW_ICON);
		});
	}

	private void startListening() {
		try {
			listener = filter(digitalPin(pinComboBox.getValue()), new EventListenerAdapter() {
				@Override
				public void stateChanged(DigitalPinValueChangedEvent event) {
					setValue(event.getValue());
				}
			});
			link.addListener(listener);
			activeButton.setText("Sensor on");
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
				activeButton.setText("Sensor off");
			}
		}
	}

	private void updateSubControlState() {
		boolean isOn = activeButton.isSelected();
		pinComboBox.setDisable(isOn);
		stateLabel.setDisable(!isOn);
		stateIcon.setOpacity(isOn ? 1.0 : 0.4);
	}

	public DigitalPinStatusPane setPin(int pin) {
		pinComboBox.getSelectionModel().select(Integer.valueOf(pin));
		return this;
	}

	@Override
	public void setLink(Link link) {
		stopListening();
		this.link = link;
	}
}
