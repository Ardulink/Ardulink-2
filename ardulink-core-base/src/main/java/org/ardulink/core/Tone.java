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

package org.ardulink.core;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Pin.AnalogPin;

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
	private final Long durationInMillis;

	private Tone(Builder builder) {
		this.analogPin = builder.analogPin;
		this.hertz = builder.hertz;
		this.durationInMillis = builder.durationInMillis;
	}

	public AnalogPin getPin() {
		return analogPin;
	}

	public int getHertz() {
		return hertz;
	}

	public Optional<Long> getDuration(TimeUnit target) {
		return Optional.ofNullable(durationInMillis).map(d -> target.convert(d, MILLISECONDS));
	}

}
