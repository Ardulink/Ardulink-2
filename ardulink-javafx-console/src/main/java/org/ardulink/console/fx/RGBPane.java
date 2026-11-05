package org.ardulink.console.fx;

import org.ardulink.core.Link;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class RGBPane extends VBox implements Linkable {

	private final PWMControlPane redPane;
	private final PWMControlPane greenPane;
	private final PWMControlPane bluePane;

	private final Pane colorSwatch;
	private final TextField colorTextField;
	private final CheckBox invertedBox;
	private boolean updatingFromTextField = false;
	private boolean updatingFromSliders = false;

	public RGBPane() {
		super(5);
		setPadding(new Insets(5));

		redPane = new PWMControlPane();
		redPane.setTitle("Red");
		redPane.setPin(2);

		greenPane = new PWMControlPane();
		greenPane.setTitle("Green");
		greenPane.setPin(1);

		bluePane = new PWMControlPane();
		bluePane.setTitle("Blue");
		bluePane.setPin(0);

		HBox controllers = new HBox(5, redPane, greenPane, bluePane);
		controllers.setAlignment(Pos.CENTER);

		colorSwatch = new Pane();
		colorSwatch.setPrefSize(150, 40);
		colorSwatch.setStyle("-fx-background-color: #000000; -fx-border-color: gray; -fx-border-width: 1;");
		colorSwatch.setCursor(javafx.scene.Cursor.HAND);

		colorTextField = new TextField("#000000");
		colorTextField.setPrefColumnCount(10);

		invertedBox = new CheckBox("Inverted");

		HBox southRow = new HBox(8, invertedBox, colorSwatch, new Label("Color:"), colorTextField);
		southRow.setAlignment(Pos.CENTER_LEFT);
		southRow.setPadding(new Insets(5));

		getChildren().addAll(controllers, southRow);

		// Wire sliders → swatch + text field
		PWMControlPane.PWMControllerListener redL = e -> {
			if (!updatingFromTextField) {
				updatingFromSliders = true;
				try {
					updateSwatchFromSliders();
				} finally {
					updatingFromSliders = false;
				}
			}
		};
		redPane.addPWMControllerListener(redL);
		greenPane.addPWMControllerListener(e -> {
			if (!updatingFromTextField) {
				updatingFromSliders = true;
				try {
					updateSwatchFromSliders();
				} finally {
					updatingFromSliders = false;
				}
			}
		});
		bluePane.addPWMControllerListener(e -> {
			if (!updatingFromTextField) {
				updatingFromSliders = true;
				try {
					updateSwatchFromSliders();
				} finally {
					updatingFromSliders = false;
				}
			}
		});

		// Wire text field → sliders + swatch
		colorTextField.textProperty().addListener((obs, old, val) -> {
			if (!updatingFromSliders) {
				updatingFromTextField = true;
				try {
					applyHexColor(val);
				} finally {
					updatingFromTextField = false;
				}
			}
		});

		// Invert checkbox
		invertedBox.selectedProperty().addListener((obs, old, val) -> {
			Color current = swatchColor();
			Color inverted = Color.rgb(
					255 - (int) (current.getRed() * 255),
					255 - (int) (current.getGreen() * 255),
					255 - (int) (current.getBlue() * 255));
			applyColor(inverted);
		});

		// Click swatch → color picker dialog
		colorSwatch.setOnMouseClicked(e -> {
			ColorPicker picker = new ColorPicker(swatchColor());
			Alert dialog = new Alert(AlertType.CONFIRMATION);
			dialog.setTitle("Choose Color");
			dialog.setHeaderText(null);
			dialog.getDialogPane().setContent(picker);
			dialog.showAndWait().filter(r -> r == ButtonType.OK)
					.ifPresent(r -> applyColor(picker.getValue()));
		});
	}

	private void updateSwatchFromSliders() {
		int r = invertedBox.isSelected() ? 255 - redPane.getValue() : redPane.getValue();
		int g = invertedBox.isSelected() ? 255 - greenPane.getValue() : greenPane.getValue();
		int b = invertedBox.isSelected() ? 255 - bluePane.getValue() : bluePane.getValue();
		Color c = Color.rgb(r, g, b);
		setSwatchColor(c);
		colorTextField.setText(toHex(c));
	}

	private void applyHexColor(String hex) {
		try {
			Color c = Color.web(hex);
			setSwatchColor(c);
			int r = (int) (c.getRed() * 255);
			int g = (int) (c.getGreen() * 255);
			int b = (int) (c.getBlue() * 255);
			if (invertedBox.isSelected()) {
				redPane.setValue(255 - r);
				greenPane.setValue(255 - g);
				bluePane.setValue(255 - b);
			} else {
				redPane.setValue(r);
				greenPane.setValue(g);
				bluePane.setValue(b);
			}
		} catch (IllegalArgumentException ignored) {
		}
	}

	private void applyColor(Color c) {
		setSwatchColor(c);
		updatingFromTextField = true;
		try {
			colorTextField.setText(toHex(c));
			int r = (int) (c.getRed() * 255);
			int g = (int) (c.getGreen() * 255);
			int b = (int) (c.getBlue() * 255);
			if (invertedBox.isSelected()) {
				redPane.setValue(255 - r);
				greenPane.setValue(255 - g);
				bluePane.setValue(255 - b);
			} else {
				redPane.setValue(r);
				greenPane.setValue(g);
				bluePane.setValue(b);
			}
		} finally {
			updatingFromTextField = false;
		}
	}

	private Color swatchColor() {
		String style = colorSwatch.getStyle();
		for (String part : style.split(";")) {
			if (part.trim().startsWith("-fx-background-color:")) {
				String hex = part.split(":")[1].trim();
				try {
					return Color.web(hex);
				} catch (IllegalArgumentException ignored) {
				}
			}
		}
		return Color.BLACK;
	}

	private void setSwatchColor(Color c) {
		colorSwatch.setStyle(String.format(
				"-fx-background-color: %s; -fx-border-color: gray; -fx-border-width: 1;", toHex(c)));
	}

	private static String toHex(Color c) {
		return String.format("#%02X%02X%02X",
				(int) (c.getRed() * 255),
				(int) (c.getGreen() * 255),
				(int) (c.getBlue() * 255));
	}

	@Override
	public void setLink(Link link) {
		redPane.setLink(link);
		greenPane.setLink(link);
		bluePane.setLink(link);
	}
}
