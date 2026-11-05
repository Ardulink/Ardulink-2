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

public class ModifiableSignalButtonPane extends TabPane implements Linkable {

	private final SignalButtonPane signalButtonPane = new SignalButtonPane();

	public ModifiableSignalButtonPane() {
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		Tab playTab = new Tab("Play", signalButtonPane);

		GridPane config = new GridPane();
		config.setHgap(5);
		config.setVgap(8);
		config.setPadding(new Insets(8));

		CheckBox valueVisible = new CheckBox("Value field is visible");
		valueVisible.setSelected(true);
		valueVisible.selectedProperty().addListener((obs, old, val) -> signalButtonPane.setValueVisible(val));
		config.add(valueVisible, 0, 0, 2, 1);

		config.add(new Label("Btn. Text:"), 0, 1);
		TextField btnText = new TextField("Send");
		btnText.textProperty().addListener((obs, old, val) -> signalButtonPane.setButtonText(val));
		config.add(btnText, 1, 1);

		config.add(new Label("Id:"), 0, 2);
		TextField idField = new TextField("ID");
		idField.textProperty().addListener((obs, old, val) -> signalButtonPane.setMessageId(val));
		config.add(idField, 1, 2);

		signalButtonPane.setButtonText("Send");
		signalButtonPane.setMessageId("ID");

		getTabs().addAll(playTab, new Tab("Configure", config));
	}

	@Override
	public void setLink(Link link) {
		signalButtonPane.setLink(link);
	}
}
