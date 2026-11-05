package org.ardulink.console.fx;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.util.Throwables;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ToggleSignalButtonPane extends VBox implements Linkable {

	private final TextField valueOnField;
	private final TextField valueOffField;
	private final HBox valueOnRow;
	private final HBox valueOffRow;
	private final ToggleButton signalButton;
	private String id = "none";
	private String buttonOnText = "On";
	private String buttonOffText = "Off";
	private Link link = NullLink.NULL_LINK;

	public ToggleSignalButtonPane() {
		super(5);
		setPadding(new Insets(5));
		setAlignment(Pos.CENTER);

		valueOnField = new TextField();
		valueOnField.setPrefColumnCount(10);
		valueOnRow = new HBox(5, new Label("Value on:"), valueOnField);
		valueOnRow.setAlignment(Pos.CENTER_LEFT);

		valueOffField = new TextField();
		valueOffField.setPrefColumnCount(10);
		valueOffRow = new HBox(5, new Label("Value off:"), valueOffField);
		valueOffRow.setAlignment(Pos.CENTER_LEFT);

		signalButton = new ToggleButton(buttonOffText);
		signalButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
			try {
				link.sendCustomMessage(getMessageId(), isSelected ? getValueOn() : getValueOff());
				signalButton.setText(isSelected ? buttonOnText : buttonOffText);
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		});

		getChildren().addAll(valueOnRow, valueOffRow, signalButton);
	}

	public String getValueOn() {
		return valueOnField.getText();
	}

	public void setValueOn(String v) {
		valueOnField.setText(v);
	}

	public String getValueOff() {
		return valueOffField.getText();
	}

	public void setValueOff(String v) {
		valueOffField.setText(v);
	}

	public boolean isValueOnVisible() {
		return valueOnRow.isVisible();
	}

	public void setValueOnVisible(boolean f) {
		valueOnRow.setVisible(f);
		valueOnRow.setManaged(f);
	}

	public boolean isValueOffVisible() {
		return valueOffRow.isVisible();
	}

	public void setValueOffVisible(boolean f) {
		valueOffRow.setVisible(f);
		valueOffRow.setManaged(f);
	}

	public void setButtonTextOn(String t) {
		buttonOnText = t;
		if (signalButton.isSelected()) {
			signalButton.setText(t);
		}
	}

	public void setButtonTextOff(String t) {
		buttonOffText = t;
		if (!signalButton.isSelected()) {
			signalButton.setText(t);
		}
	}

	public String getMessageId() {
		return id;
	}

	public void setMessageId(String id) {
		this.id = id;
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}
}
