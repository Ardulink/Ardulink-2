package com.github.pfichtner;

public abstract class Pin {

	public abstract int pinNum();

	public static class AnalogPin extends Pin {

		private final int num;

		private AnalogPin(int num) {
			this.num = num;
		}

		public int pinNum() {
			return this.num;
		}

	}

	public static class DigitalPin extends Pin {

		private final int num;

		private DigitalPin(int num) {
			this.num = num;
		}

		public int pinNum() {
			return this.num;
		}

	}

	public static AnalogPin analogPin(int num) {
		return new AnalogPin(num);
	}

	public static DigitalPin digitalPin(int num) {
		return new DigitalPin(num);
	}

}
