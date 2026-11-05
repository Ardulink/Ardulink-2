package org.ardulink.console.fx;

import org.ardulink.core.Link;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ModifiableToggleSignalButtonPane extends TabPane implements Linkable {

	private final ToggleSignalButtonPane signalButtonPane = new ToggleSignalButtonPane();

	public ModifiableToggleSignalButtonPane() {
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		Tab playTab = new Tab("Play", signalButtonPane);

		GridPane config = new GridPane();
		config.setHgap(5);
		config.setVgap(8);
		config.setPadding(new Insets(8));

		CheckBox valueOnVisible = new CheckBox("Value field ON is visible");
		valueOnVisible.setSelected(true);
		valueOnVisible.selectedProperty().addListener((obs, old, val) -> signalButtonPane.setValueOnVisible(val));
		config.add(valueOnVisible, 0, 0, 2, 1);

		CheckBox valueOffVisible = new CheckBox("Value field OFF is visible");
		valueOffVisible.setSelected(true);
		valueOffVisible.selectedProperty().addListener((obs, old, val) -> signalButtonPane.setValueOffVisible(val));
		config.add(valueOffVisible, 0, 1, 2, 1);

		config.add(new Label("Btn. ON Text:"), 0, 2);
		TextField btnOnText = new TextField("On");
		btnOnText.textProperty().addListener((obs, old, val) -> signalButtonPane.setButtonTextOn(val));
		config.add(btnOnText, 1, 2);

		config.add(new Label("Btn. OFF Text:"), 0, 3);
		TextField btnOffText = new TextField("Off");
		btnOffText.textProperty().addListener((obs, old, val) -> signalButtonPane.setButtonTextOff(val));
		config.add(btnOffText, 1, 3);

		config.add(new Label("Id:"), 0, 4);
		TextField idField = new TextField("ID");
		idField.textProperty().addListener((obs, old, val) -> signalButtonPane.setMessageId(val));
		config.add(idField, 1, 4);

		signalButtonPane.setButtonTextOn("On");
		signalButtonPane.setButtonTextOff("Off");
		signalButtonPane.setMessageId("ID");

		getTabs().addAll(playTab, new Tab("Configure", config));
	}

	@Override
	public void setLink(Link link) {
		signalButtonPane.setLink(link);
	}
}
