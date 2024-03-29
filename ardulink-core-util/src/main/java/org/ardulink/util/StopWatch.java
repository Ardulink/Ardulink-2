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
package org.ardulink.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.TimeUnit;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class StopWatch {

	private static class StoppedStopWatch extends StopWatch {

		@Override
		public StopWatch start() {
			return new StartedStopWatch();
		}

		@Override
		public boolean isStarted() {
			return false;
		}

		@Override
		public long getTime() {
			return 0;
		}

	}

	private static class StartedStopWatch extends StopWatch {

		private final long startedAt = now();

		private long now() {
			return System.currentTimeMillis();
		}

		@Override
		public StopWatch start() {
			throw new IllegalStateException("StopWatch already started");
		}

		@Override
		public boolean isStarted() {
			return true;
		}

		@Override
		public long getTime() {
			return now() - startedAt;
		}
	}

	public static StopWatch createStarted() {
		return createUnstarted().start();
	}

	private static StopWatch createUnstarted() {
		return new StoppedStopWatch();
	}

	public static class Countdown {

		private final int amount;
		private final TimeUnit timeUnit;
		private final StopWatch stopWatch;

		public static Countdown createStarted(int amount, TimeUnit timeUnit) {
			return new Countdown(amount, timeUnit, StopWatch.createStarted());
		}

		public static Countdown createUnstarted(int amount, TimeUnit timeUnit) {
			return new Countdown(amount, timeUnit, StopWatch.createUnstarted());
		}

		public Countdown(int amount, TimeUnit timeUnit, StopWatch stopWatch) {
			this.amount = amount;
			this.timeUnit = timeUnit;
			this.stopWatch = stopWatch;
		}

		public StopWatch start() {
			return stopWatch.start();
		}

		public boolean isStarted() {
			return stopWatch.isStarted();
		}

		public long remaining(TimeUnit timeUnit) {
			return timeUnit.convert(amount - stopWatch.getTime(timeUnit), this.timeUnit);
		}

		public boolean finished() {
			return stopWatch.elapsed(amount, timeUnit);
		}

	}

	public abstract StopWatch start();

	public abstract boolean isStarted();

	public abstract long getTime();

	public long getTime(TimeUnit timeUnit) {
		return timeUnit.convert(getTime(), MILLISECONDS);
	}

	public boolean elapsed(int compareTo, TimeUnit timeUnit) {
		return getTime(timeUnit) >= compareTo;
	}

}
