package org.ardulink.console.fx;

import org.ardulink.core.Link;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ModifiableJoystickPane extends BorderPane implements Linkable {

	private final JoystickCanvas joystick;
	private final Label horizontalLabel;
	private final Label verticalLabel;

	public ModifiableJoystickPane() {
		joystick = new JoystickCanvas(180, 180);
		setCenter(joystick);

		horizontalLabel = new Label("Horizontal: 0");
		verticalLabel = new Label("Vertical: 0");
		joystick.addPositionListener(event -> {
			horizontalLabel.setText("Horizontal: " + event.position().x);
			verticalLabel.setText("Vertical: " + event.position().y);
		});

		VBox coordBox = new VBox(3, horizontalLabel, verticalLabel);
		setTop(coordBox);

		GridPane controls = new GridPane();
		controls.setHgap(5);
		controls.setVgap(4);
		controls.setPadding(new Insets(4));

		controls.add(new Label("ID:"), 0, 0);
		TextField idField = new TextField("none");
		idField.textProperty().addListener((obs, old, val) -> joystick.setJoystickId(val));
		idField.setPrefColumnCount(8);
		controls.add(idField, 1, 0);

		controls.add(new Label("Max Value:"), 0, 1);
		Spinner<Integer> maxSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 255));
		maxSpinner.valueProperty().addListener((obs, old, val) -> joystick.setJoyOutputRange(val));
		maxSpinner.setPrefWidth(80);
		controls.add(maxSpinner, 1, 1);

		setBottom(controls);
	}

	public boolean addPositionListener(PositionListener listener) {
		return joystick.addPositionListener(listener);
	}

	public ModifiableJoystickPane setJoystickId(String id) {
		joystick.setJoystickId(id);
		return this;
	}

	@Override
	public void setLink(Link link) {
		joystick.setLink(link);
	}
}
