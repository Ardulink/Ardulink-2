/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.github.pfichtner.ardulink.core;

import java.util.concurrent.TimeUnit;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
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
