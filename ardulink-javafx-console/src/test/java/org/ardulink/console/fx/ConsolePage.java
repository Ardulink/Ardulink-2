package org.ardulink.console.fx;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.ardulink.core.Link;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

class ConsolePage implements AutoCloseable {

	private final Stage stage;

	ConsolePage(Supplier<Link> linkFactory) throws Exception {
		CompletableFuture<Stage> stageFuture = new CompletableFuture<>();
		Platform.runLater(() -> {
			try {
				Stage s = new Stage();
				new ConsoleWindow(s) {
					@Override
					protected Link createLink() {
						return linkFactory.get();
					}
				}.show();
				stageFuture.complete(s);
			} catch (Throwable t) {
				stageFuture.completeExceptionally(t);
			}
		});
		this.stage = stageFuture.get(20, SECONDS);
	}

	BufferedImage snapshot() throws Exception {
		return runFx(() -> fxImageToBufferedImage(stage.getScene().snapshot(null)));
	}

	private static BufferedImage fxImageToBufferedImage(WritableImage img) {
		int w = (int) img.getWidth();
		int h = (int) img.getHeight();
		int[] pixels = new int[w * h];
		img.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), pixels, 0, w);
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		out.getRaster().setDataElements(0, 0, w, h, pixels);
		return out;
	}

	boolean isConnectEnabled() throws Exception {
		return runFx(() -> !findButton("Connect").isDisabled());
	}

	boolean isDisconnectEnabled() throws Exception {
		return runFx(() -> !findButton("Disconnect").isDisabled());
	}

	void connect() throws Exception {
		runFx(() -> findButton("Connect").fire());
	}

	void disconnect() throws Exception {
		runFx(() -> findButton("Disconnect").fire());
	}

	@Override
	public void close() throws Exception {
		runFx(() -> {}); // flush any pending Platform.runLater tasks (e.g. connected.set)
		runFx(() -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
	}

	private Button findButton(String text) {
		return findNode(stage.getScene().getRoot(), Button.class, btn -> text.equals(btn.getText()));
	}

	private <T> T runFx(Callable<T> task) throws Exception {
		CompletableFuture<T> f = new CompletableFuture<>();
		Platform.runLater(() -> {
			try {
				f.complete(task.call());
			} catch (Throwable t) {
				f.completeExceptionally(t);
			}
		});
		return f.get(10, SECONDS);
	}

	void runFx(Runnable task) throws Exception {
		runFx(() -> {
			task.run();
			return null;
		});
	}

	static <T extends Node> T findNode(Parent root, Class<T> type, Predicate<T> pred) {
		for (Node child : root.getChildrenUnmodifiable()) {
			if (type.isInstance(child) && pred.test(type.cast(child))) {
				return type.cast(child);
			}
			if (child instanceof Parent) {
				T r = findNode((Parent) child, type, pred);
				if (r != null) {
					return r;
				}
			}
		}
		return null;
	}
}
