package org.ardulink.console.fx;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.util.Throwables;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class KeyControlPane extends VBox implements Linkable {

	private final Label keyInfoLabel;
	private Link link = NullLink.NULL_LINK;

	public KeyControlPane() {
		super(10);
		setPadding(new Insets(10));
		setAlignment(Pos.CENTER);

		Label hint = new Label("Press any key");
		hint.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

		keyInfoLabel = new Label("");
		keyInfoLabel.setStyle("-fx-font-size: 11;");
		keyInfoLabel.setWrapText(true);

		getChildren().addAll(hint, keyInfoLabel);

		setFocusTraversable(true);
		setOnKeyPressed(this::handleKeyPressed);
		setOnKeyReleased(e -> keyInfoLabel.setText(""));
	}

	private void handleKeyPressed(KeyEvent event) {
		String info = "Char: " + event.getCharacter()
				+ " - Code: " + event.getCode()
				+ " - Text: " + event.getText();
		keyInfoLabel.setText(info);
		try {
			char keyChar = event.getCharacter().isEmpty() ? '\0' : event.getCharacter().charAt(0);
			link.sendKeyPressEvent(keyChar, event.getCode().getCode(), 1, 0, 0);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}
}
