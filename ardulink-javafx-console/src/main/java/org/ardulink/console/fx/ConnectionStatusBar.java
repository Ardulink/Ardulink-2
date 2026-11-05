package org.ardulink.console.fx;

import org.ardulink.core.ConnectionListener;
import org.ardulink.core.Link;
import org.ardulink.core.NullLink;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ConnectionStatusBar extends HBox implements Linkable {

	private static final Image CONNECTED_ICON = loadIcon("icons/connect_established.png");
	private static final Image DISCONNECTED_ICON = loadIcon("icons/connect_no.png");

	private final ImageView iconView;
	private final Label statusLabel;
	private Link link = NullLink.NULL_LINK;

	private final ConnectionListener connectionListener = new ConnectionListener() {
		@Override
		public void connectionLost() {
			Platform.runLater(() -> {
				iconView.setImage(DISCONNECTED_ICON);
				statusLabel.setText("disconnected");
			});
		}

		@Override
		public void reconnected() {
			Platform.runLater(() -> {
				iconView.setImage(CONNECTED_ICON);
				statusLabel.setText("connected");
			});
		}
	};

	public ConnectionStatusBar() {
		super(5);
		setAlignment(Pos.CENTER_LEFT);
		setPadding(new Insets(3, 8, 3, 8));

		iconView = new ImageView(DISCONNECTED_ICON);
		iconView.setFitWidth(16);
		iconView.setFitHeight(16);
		iconView.setPreserveRatio(true);

		statusLabel = new Label("disconnected");

		getChildren().addAll(iconView, statusLabel);
	}

	private static Image loadIcon(String path) {
		return new Image(ConnectionStatusBar.class.getResourceAsStream(path));
	}

	@Override
	public void setLink(Link newLink) {
		this.link = LinkReplacer.replace(this.link, newLink, connectionListener);
	}
}
