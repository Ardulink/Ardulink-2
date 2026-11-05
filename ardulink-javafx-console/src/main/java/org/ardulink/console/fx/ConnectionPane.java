package org.ardulink.console.fx;

import java.net.URI;
import java.util.List;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.Configurer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ConnectionPane extends VBox {

	private final ComboBox<URI> uriComboBox;
	private final BorderPane subPanelArea;
	private Configurer configurer;

	public ConnectionPane() {
		super(6);
		setPadding(new Insets(8));

		uriComboBox = new ComboBox<>(FXCollections.observableArrayList());
		uriComboBox.setConverter(new StringConverter<>() {
			@Override
			public String toString(URI uri) {
				return uri == null ? "" : LinkManager.extractNameFromURI(uri);
			}

			@Override
			public URI fromString(String s) {
				return null;
			}
		});
		HBox.setHgrow(uriComboBox, Priority.ALWAYS);

		Button refreshButton = new Button("Refresh");
		refreshButton.setOnAction(__ -> loadSubPanel());

		HBox typeRow = new HBox(5, new Label("Type:"), uriComboBox, refreshButton);
		typeRow.setAlignment(Pos.CENTER_LEFT);

		subPanelArea = new BorderPane();

		getChildren().addAll(typeRow, subPanelArea);

		uriComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, uri) -> {
			if (uri != null) {
				loadSubPanel();
			}
		});

		Thread uriLoader = new Thread(() -> {
			List<URI> uris = LinkManager.getInstance().listURIs();
			Platform.runLater(() -> {
				if (!windowIsShowing()) {
					return;
				}
				uriComboBox.setItems(FXCollections.observableArrayList(uris));
				if (!uris.isEmpty()) {
					uriComboBox.getSelectionModel().selectFirst();
				}
			});
		});
		uriLoader.setDaemon(true);
		uriLoader.start();
	}

	private boolean windowIsShowing() {
		return subPanelArea.getScene() != null
				&& subPanelArea.getScene().getWindow() != null
				&& subPanelArea.getScene().getWindow().isShowing();
	}

	private void loadSubPanel() {
		URI uri = uriComboBox.getValue();
		if (uri == null || !windowIsShowing()) {
			return;
		}
		ProgressIndicator progress = new ProgressIndicator();
		progress.setPrefSize(32, 32);
		subPanelArea.setCenter(progress);

		Thread loader = new Thread(() -> {
			try {
				Configurer conf = LinkManager.getInstance().getConfigurer(uri);
				Platform.runLater(() -> {
					if (subPanelArea.getScene() == null
							|| subPanelArea.getScene().getWindow() == null
							|| !subPanelArea.getScene().getWindow().isShowing()) {
						return;
					}
					configurer = conf;
					try {
						GridPane panel = new GenericConfigPane(conf);
						ScrollPane scroll = new ScrollPane(panel);
						scroll.setFitToWidth(true);
						subPanelArea.setCenter(scroll);
					} catch (Exception e) {
						showError(e);
					}
				});
			} catch (Exception e) {
				Platform.runLater(() -> showError(e));
			}
		});
		loader.setDaemon(true);
		loader.start();
	}

	private void showError(Exception e) {
		Throwable cause = e;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Connection Error");
		alert.setHeaderText("Failed to load connection panel");
		alert.setContentText(cause.getClass().getName() + ": " + cause.getMessage());
		alert.show();
		subPanelArea.setCenter(null);
	}

	public Link createLink() {
		if (configurer == null) {
			return null;
		}
		try {
			return configurer.newLink();
		} catch (Exception e) {
			showError(e);
			return null;
		}
	}
}
