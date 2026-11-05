package org.ardulink.console.fx;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.Link;
import org.ardulink.core.NullLink;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class JoystickCanvas extends Canvas implements Linkable {

	private static final int BALL_RADIUS = 10;
	private static final int BORDER = 20;

	private int joyOutputRange = 255;
	private String id = "none";
	private Link link = NullLink.NULL_LINK;
	private final List<PositionListener> positionListeners = new CopyOnWriteArrayList<>();

	private double ballX = 0;
	private double ballY = 0;
	private boolean tracking = false;

	public JoystickCanvas() {
		this(200, 200);
	}

	public JoystickCanvas(double width, double height) {
		super(width, height);
		setOnMousePressed(this::onMousePressed);
		setOnMouseDragged(this::onMouseDragged);
		setOnMouseReleased(this::onMouseReleased);
		widthProperty().addListener(__ -> draw());
		heightProperty().addListener(__ -> draw());
		draw();
	}

	private void onMousePressed(MouseEvent e) {
		tracking = e.getButton() == MouseButton.PRIMARY;
		updatePosition(e.getX(), e.getY());
	}

	private void onMouseDragged(MouseEvent e) {
		updatePosition(e.getX(), e.getY());
	}

	private void onMouseReleased(MouseEvent e) {
		tracking = false;
		ballX = 0;
		ballY = 0;
		draw();
		firePositionEvent(0, 0);
		sendMessage(0, 0);
	}

	private void updatePosition(double mouseX, double mouseY) {
		if (!tracking) {
			return;
		}
		double cx = getWidth() / 2;
		double cy = getHeight() / 2;
		double radius = joystickRadius();

		double dx = mouseX - cx;
		double dy = mouseY - cy;
		double dist = sqrt(dx * dx + dy * dy);
		double angle = atan2(dy, dx);
		double clampedDist = min(dist, radius);

		ballX = clampedDist * cos(angle);
		ballY = clampedDist * sin(angle);

		int outX = (int) (joyOutputRange * ballX / radius);
		int outY = (int) (joyOutputRange * -ballY / radius);

		draw();
		firePositionEvent(outX, outY);
		sendMessage(outX, outY);
	}

	private double joystickRadius() {
		return (min(getWidth(), getHeight()) - BORDER) / 2.0;
	}

	private void draw() {
		GraphicsContext gc = getGraphicsContext2D();
		double w = getWidth();
		double h = getHeight();
		double cx = w / 2;
		double cy = h / 2;
		double radius = joystickRadius();

		gc.clearRect(0, 0, w, h);

		gc.setFill(Color.LIGHTGRAY);
		gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

		gc.setStroke(Color.DARKGRAY);
		gc.setLineWidth(2);
		gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);

		double bx = cx + ballX;
		double by = cy + ballY;

		if (tracking && (ballX != 0 || ballY != 0)) {
			gc.setStroke(Color.GRAY);
			gc.setLineWidth(8);
			gc.strokeLine(cx, cy, bx, by);
		}

		gc.setFill(Color.GRAY);
		gc.fillOval(bx - BALL_RADIUS, by - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
	}

	private void firePositionEvent(int x, int y) {
		PositionEvent event = new PositionEvent(new PositionEvent.Point(x, y), joyOutputRange, getJoystickId());
		positionListeners.forEach(l -> l.positionChanged(event));
	}

	private void sendMessage(int x, int y) {
		try {
			link.sendCustomMessage(getJoystickId(), String.valueOf(x), String.valueOf(y));
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	public boolean addPositionListener(PositionListener listener) {
		return positionListeners.add(listener);
	}

	public boolean removePositionListener(PositionListener listener) {
		return positionListeners.remove(listener);
	}

	public String getJoystickId() {
		return id;
	}

	public void setJoystickId(String id) {
		this.id = id;
	}

	public int getJoyOutputRange() {
		return joyOutputRange;
	}

	public void setJoyOutputRange(int range) {
		this.joyOutputRange = range;
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public double prefWidth(double height) {
		return getWidth();
	}

	@Override
	public double prefHeight(double width) {
		return getHeight();
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}
}
