package org.ardulink.console.fx;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.util.Throwables;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SignalButtonPane extends VBox implements Linkable {

	private final TextField valueField;
	private final HBox valueRow;
	private final Button sendButton;
	private String id = "none";
	private Link link = NullLink.NULL_LINK;

	public SignalButtonPane() {
		super(5);
		setPadding(new Insets(5));
		setAlignment(Pos.CENTER);

		valueField = new TextField();
		valueField.setPrefColumnCount(10);
		valueRow = new HBox(5, new Label("Value:"), valueField);
		valueRow.setAlignment(Pos.CENTER_LEFT);

		sendButton = new Button("Send");
		sendButton.setOnAction(__ -> {
			try {
				link.sendCustomMessage(getMessageId(), getValue());
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		});

		getChildren().addAll(valueRow, sendButton);
	}

	public String getValue() {
		return valueField.getText();
	}

	public void setValue(String value) {
		valueField.setText(value);
	}

	public boolean isValueVisible() {
		return valueRow.isVisible();
	}

	public void setValueVisible(boolean flag) {
		valueRow.setVisible(flag);
		valueRow.setManaged(flag);
	}

	public void setButtonText(String text) {
		sendButton.setText(text);
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
