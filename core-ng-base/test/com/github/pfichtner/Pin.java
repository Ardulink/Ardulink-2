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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + num;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnalogPin other = (AnalogPin) obj;
			if (num != other.num)
				return false;
			return true;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + num;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DigitalPin other = (DigitalPin) obj;
			if (num != other.num)
				return false;
			return true;
		}

	}

	public static AnalogPin analogPin(int num) {
		return new AnalogPin(num);
	}

	public static DigitalPin digitalPin(int num) {
		return new DigitalPin(num);
	}

}
