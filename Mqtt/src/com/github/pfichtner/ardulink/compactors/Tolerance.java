package com.github.pfichtner.ardulink.compactors;

import static java.lang.Math.abs;

public class Tolerance {

	private final int maxTolerance;

	public Tolerance(int maxTolerance) {
		this.maxTolerance = maxTolerance;
	}

	public static Tolerance maxTolerance(int maxTolerance) {
		return new Tolerance(maxTolerance);
	}

	public boolean isZero() {
		return maxTolerance == 0;
	}

	protected boolean inTolerance(int oldValue, int newValue) {
		return abs(oldValue - newValue) <= maxTolerance;
	}

}