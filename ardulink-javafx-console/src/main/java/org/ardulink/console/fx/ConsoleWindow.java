package org.ardulink.console.fx;

import static java.util.stream.IntStream.rangeClosed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ardulink.core.ConnectionListener;
import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.util.Throwables;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConsoleWindow {

	private final Stage stage;
	private final BooleanProperty connected = new SimpleBooleanProperty(false);
	private final List<Linkable> linkables = new ArrayList<>();
	private Link link = NullLink.NULL_LINK;

	private final ConnectionListener connectionListener = new ConnectionListener() {
		@Override
		public void connectionLost() {
			Platform.runLater(() -> connected.set(false));
		}

		@Override
		public void reconnected() {
			Platform.runLater(() -> connected.set(true));
		}
	};

	private final ConnectionPane connectionPane = new ConnectionPane();
	private final ConnectionStatusBar statusBar = new ConnectionStatusBar();
	private final KeyControlPane keyControlPane = new KeyControlPane();

	public ConsoleWindow(Stage stage) {
		this.stage = stage;
		buildScene();
	}

	private void buildScene() {
		stage.setTitle("Ardulink Console");
		stage.getIcons().add(loadIcon("icons/logo_icon.png"));

		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		// --- Configuration tab (always interactive) ---
		Button btnConnect = new Button("Connect");
		btnConnect.setOnAction(__ -> connect());
		btnConnect.disableProperty().bind(connected);

		Button btnDisconnect = new Button("Disconnect");
		btnDisconnect.setOnAction(__ -> disconnect());
		btnDisconnect.disableProperty().bind(connected.not());

		connectionPane.disableProperty().bind(connected);
		HBox connectButtons = new HBox(5, btnConnect, btnDisconnect);
		connectButtons.setAlignment(Pos.CENTER);
		connectButtons.setPadding(new Insets(5));
		VBox configContent = new VBox(connectionPane, connectButtons);
		tabPane.getTabs().add(new Tab("Configuration", configContent));

		// --- Key Control tab ---
		Tab keyTab = disabledTab("Key Control", keyControlPane);
		tabPane.getTabs().add(keyTab);
		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, old, newTab) -> {
			if (newTab == keyTab) {
				javafx.application.Platform.runLater(keyControlPane::requestFocus);
			}
		});

		// --- Power Panel (pins 3–11, 3 columns) ---
		List<PWMControlPane> pwmPanes = rangeClosed(3, 11)
				.mapToObj(pin -> new PWMControlPane().setPin(pin))
				.collect(Collectors.toList());
		tabPane.getTabs().add(disabledTab("Power Panel", tileGrid(3, pwmPanes)));

		// --- Switch Panel (pins 3–13, 3 columns) ---
		List<SwitchControlPane> switchPanes = rangeClosed(3, 13)
				.mapToObj(pin -> new SwitchControlPane().setPin(pin))
				.collect(Collectors.toList());
		tabPane.getTabs().add(disabledTab("Switch Panel", tileGrid(3, switchPanes)));

		// --- Joystick Panel ---
		List<ModifiableJoystickPane> joysticks = new ArrayList<>();
		for (String joystickId : new String[] { "Left", "Right" }) {
			ModifiableJoystickPane joy = new ModifiableJoystickPane().setJoystickId(joystickId);
			joy.addPositionListener(new SimplePositionIndicator());
			joysticks.add(joy);
		}
		tabPane.getTabs().add(disabledTab("Joystick Panel", tileGrid(2, joysticks)));

		// --- Digital Sensor Panel (pins 2–12, 3 columns) ---
		List<DigitalPinStatusPane> digitalPanes = rangeClosed(2, 12)
				.mapToObj(pin -> new DigitalPinStatusPane().setPin(pin))
				.collect(Collectors.toList());
		tabPane.getTabs().add(disabledTab("Digital Sensor Panel", tileGrid(3, digitalPanes)));

		// --- Analog Sensor Panel (pins 0–5, 3 columns) ---
		List<AnalogPinStatusPane> analogPanes = rangeClosed(0, 5)
				.mapToObj(pin -> new AnalogPinStatusPane().setPin(pin))
				.collect(Collectors.toList());
		tabPane.getTabs().add(disabledTab("Analog Sensor Panel", tileGrid(3, analogPanes)));

		// --- Custom Panel (3 signal + 3 toggle) ---
		List<javafx.scene.Node> customNodes = new ArrayList<>();
		List<Linkable> customLinkables = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			ModifiableSignalButtonPane p = new ModifiableSignalButtonPane();
			customNodes.add(p);
			customLinkables.add(p);
		}
		for (int i = 0; i < 3; i++) {
			ModifiableToggleSignalButtonPane p = new ModifiableToggleSignalButtonPane();
			customNodes.add(p);
			customLinkables.add(p);
		}
		tabPane.getTabs().add(disabledTab("Custom Components", tileGrid(3, customNodes)));

		// --- Tone Panel ---
		TonePane tonePane = new TonePane();
		tabPane.getTabs().add(disabledTab("Tone Panel", tonePane));

		// --- RGB Panel ---
		RGBPane rgbPane = new RGBPane();
		tabPane.getTabs().add(disabledTab("RGB Panel", rgbPane));

		// --- Monitor Panel ---
		SerialMonitorPane monitorPane = new SerialMonitorPane();
		tabPane.getTabs().add(disabledTab("Monitor Panel", monitorPane));

		// Register all Linkable components
		linkables.add(keyControlPane);
		pwmPanes.forEach(linkables::add);
		switchPanes.forEach(linkables::add);
		joysticks.forEach(linkables::add);
		digitalPanes.forEach(linkables::add);
		analogPanes.forEach(linkables::add);
		linkables.addAll(customLinkables);
		linkables.add(tonePane);
		linkables.add(rgbPane);
		linkables.add(monitorPane);
		linkables.add(statusBar);

		// Status bar
		HBox statusRow = new HBox(statusBar);
		statusRow.setStyle("-fx-border-color: lightgray; -fx-border-width: 1 0 0 0;");

		BorderPane root = new BorderPane();
		root.setCenter(tabPane);
		root.setBottom(statusRow);

		stage.setScene(new Scene(root, 760, 650));
		stage.setOnCloseRequest(e -> {
			disconnect();
			stage.close();
		});

		setLink(NullLink.NULL_LINK);
	}

	private Tab disabledTab(String title, javafx.scene.Node content) {
		Tab tab = new Tab(title, content);
		tab.disableProperty().bind(connected.not());
		return tab;
	}

	private static TilePane tileGrid(int columns, List<? extends javafx.scene.Node> nodes) {
		TilePane pane = new TilePane();
		pane.setPrefColumns(columns);
		pane.setHgap(5);
		pane.setVgap(5);
		pane.setPadding(new Insets(5));
		pane.getChildren().addAll(nodes);
		return pane;
	}

	protected Link createLink() {
		return connectionPane.createLink();
	}

	private void connect() {
		if (!NullLink.isNullLink(link)) {
			try {
				link.close();
			} catch (IOException e) {
				showError("Error closing previous link", e);
			}
		}
		Link newLink = createLink();
		if (newLink != null) {
			setLink(newLink);
		}
	}

	private void disconnect() {
		if (!NullLink.isNullLink(link)) {
			try {
				link.close();
			} catch (IOException e) {
				showError("Disconnect error", e);
			}
		}
		setLink(NullLink.NULL_LINK);
	}

	private void setLink(Link newLink) {
		link = LinkReplacer.replace(this.link, newLink, connectionListener);
		linkables.forEach(l -> l.setLink(link));
	}

	private void showError(String header, Throwable e) {
		Throwable cause = e;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(header);
		alert.setContentText(cause.getClass().getName() + ": " + cause.getMessage());
		alert.showAndWait();
	}

	private static Image loadIcon(String path) {
		return new Image(ConsoleWindow.class.getResourceAsStream(path));
	}

	public void show() {
		stage.show();
	}

	/** Minimal position indicator used for joystick panels. */
	private static class SimplePositionIndicator implements PositionListener {
		@Override
		public void positionChanged(PositionEvent event) {
			// Coordinates are shown in ModifiableJoystickPane's own labels; no extra action needed.
		}
	}
}
