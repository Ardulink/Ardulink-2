package com.github.pfichtner;

public abstract class Pin {

	private final int num;

	protected Pin(int num) {
		this.num = num;
	}

	public int pinNum() {
		return num;
	}

	public static class AnalogPin extends Pin {
		private AnalogPin(int num) {
			super(num);
		}
	}

	public static class DigitalPin extends Pin {
		private DigitalPin(int num) {
			super(num);
		}
	}

	public static AnalogPin analogPin(int num) {
		return new AnalogPin(num);
	}

	public static DigitalPin digitalPin(int num) {
		return new DigitalPin(num);
	}

}
