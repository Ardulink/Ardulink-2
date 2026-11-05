package org.ardulink.console.fx;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;

import org.ardulink.core.Connection.Listener;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.NullLink;
import org.ardulink.core.qos.QosLink;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SerialMonitorPane extends BorderPane implements Linkable {

	private final TextArea sentArea;
	private final TextArea receivedArea;
	private final TextField messageField;
	private ConnectionBasedLink connectionLink;

	private final Listener connectionListener = new Listener() {
		@Override
		public void received(byte[] bytes) {
			Platform.runLater(() -> receivedArea.appendText("\n" + new String(bytes, UTF_8)));
		}

		@Override
		public void sent(byte[] bytes) {
			Platform.runLater(() -> sentArea.appendText(new String(bytes, UTF_8)));
		}
	};

	public SerialMonitorPane() {
		setPadding(new Insets(5));

		messageField = new TextField();
		HBox.setHgrow(messageField, Priority.ALWAYS);
		Button sendButton = new Button("Send");
		sendButton.setOnAction(__ -> {
			if (connectionLink != null) {
				try {
					connectionLink.getConnection().write((messageField.getText() + "\n").getBytes(UTF_8));
				} catch (IOException e) {
					throw propagate(e);
				}
			}
		});
		HBox sendRow = new HBox(5, messageField, sendButton);
		sendRow.setAlignment(Pos.CENTER);
		setTop(sendRow);

		sentArea = readOnlyTextArea();
		receivedArea = readOnlyTextArea();

		VBox sentBox = labeled("Sent:", sentArea, new Button("Clear Sent") {{
			setOnAction(__ -> sentArea.clear());
		}});
		VBox receivedBox = labeled("Received:", receivedArea, new Button("Clear Received") {{
			setOnAction(__ -> receivedArea.clear());
		}});

		SplitPane split = new SplitPane(sentBox, receivedBox);
		split.setOrientation(Orientation.VERTICAL);
		split.setDividerPositions(0.5);
		setCenter(split);
	}

	private static TextArea readOnlyTextArea() {
		TextArea area = new TextArea();
		area.setEditable(false);
		area.setWrapText(false);
		return area;
	}

	private static VBox labeled(String title, TextArea area, Button clearButton) {
		FlowPane buttons = new FlowPane(clearButton);
		buttons.setAlignment(Pos.CENTER_RIGHT);
		VBox box = new VBox(3, new Label(title), area, buttons);
		VBox.setVgrow(area, Priority.ALWAYS);
		return box;
	}

	@Override
	public void setLink(Link link) {
		if (connectionLink != null) {
			connectionLink.getConnection().removeListener(connectionListener);
			connectionLink = null;
		}
		sentArea.clear();
		receivedArea.clear();

		Link unwrapped = link instanceof QosLink ? ((QosLink) link).getDelegate() : link;
		if (unwrapped instanceof ConnectionBasedLink && !NullLink.isNullLink(unwrapped)) {
			connectionLink = (ConnectionBasedLink) unwrapped;
			connectionLink.getConnection().addListener(connectionListener);
		}
	}
}
