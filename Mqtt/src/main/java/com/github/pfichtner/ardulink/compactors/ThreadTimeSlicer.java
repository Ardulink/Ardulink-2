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
package com.github.pfichtner.ardulink.compactors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ThreadTimeSlicer implements TimeSlicer {

	private final List<SlicedAnalogReadChangeListenerAdapter> runnables = new ArrayList<SlicedAnalogReadChangeListenerAdapter>();

	public ThreadTimeSlicer(final long value, final TimeUnit timeUnit) {
		new Thread() {
			{
				setDaemon(true);
				start();
			}

			@Override
			public void run() {
				while (true) {
					try {
						timeUnit.sleep(value);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					for (SlicedAnalogReadChangeListenerAdapter runnable : runnables) {
						runnable.ticked();
					}
				}
			}
		};
	}

	@Override
	public void add(SlicedAnalogReadChangeListenerAdapter runnable) {
		this.runnables.add(runnable);
	};

}
