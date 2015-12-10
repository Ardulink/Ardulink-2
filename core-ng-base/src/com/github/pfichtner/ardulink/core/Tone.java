package com.github.pfichtner.ardulink.core;

import java.util.concurrent.TimeUnit;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;

public class Tone {

	public static class Builder {

		private AnalogPin analogPin;
		private int hertz;
		private Long durationInMillis;

		public Builder(AnalogPin analogPin) {
			this.analogPin = analogPin;
		}

		public Builder withHertz(int hertz) {
			this.hertz = hertz;
			return this;
		}

		public Tone endless() {
			return new Tone(this);
		}

		public Tone withDuration(int duration, TimeUnit timeUnit) {
			this.durationInMillis = timeUnit.toMillis(duration);
			return new Tone(this);
		}

	}

	public static Builder forPin(AnalogPin analogPin) {
		return new Builder(analogPin);
	}

	private final int hertz;
	private final AnalogPin analogPin;
	private final Long duration;

	public Tone(Builder builder) {
		this.analogPin = builder.analogPin;
		this.hertz = builder.hertz;
		this.duration = builder.durationInMillis;
	}

	public AnalogPin getPin() {
		return analogPin;
	}

	public int getHertz() {
		return hertz;
	}

	public Long getDurationInMillis() {
		return duration;
	}

}
