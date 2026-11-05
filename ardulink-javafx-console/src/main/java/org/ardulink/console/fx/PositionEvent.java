package org.ardulink.console.fx;

public class PositionEvent {

	public static class Point {

		public static final Point NULL = new Point(0, 0);

		public final int x;
		public final int y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private final Point position;
	private final int maxSize;
	private final String id;

	public PositionEvent(Point position, int maxSize, String id) {
		this.position = position;
		this.maxSize = maxSize;
		this.id = id;
	}

	public Point position() {
		return position;
	}

	public int maxSize() {
		return maxSize;
	}

	public String id() {
		return id;
	}
}
