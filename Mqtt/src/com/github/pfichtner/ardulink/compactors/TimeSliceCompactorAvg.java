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

import static com.github.pfichtner.ardulink.util.Integers.average;

import java.util.List;
import java.util.Map.Entry;

import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;

import com.github.pfichtner.ardulink.util.HashMultiMap;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class TimeSliceCompactorAvg extends
		SlicedAnalogReadChangeListenerAdapter {

	private boolean firstCall = true;
	private final HashMultiMap<Integer, Integer> data = new HashMultiMap<Integer, Integer>();

	public TimeSliceCompactorAvg(AnalogReadChangeListener delegate) {
		super(delegate);
	}

	@Override
	public void stateChanged(AnalogReadChangeEvent event) {
		if (this.firstCall) {
			getDelegate().stateChanged(event);
			this.firstCall = false;
		} else {
			synchronized (this.data) {
				this.data.put(event.getPin(), event.getValue());
			}
		}
	}

	@Override
	public void ticked() {
		synchronized (this.data) {
			if (!data.isEmpty()) {
				for (Entry<Integer, List<Integer>> entry : data.asMap()
						.entrySet()) {
					getDelegate()
							.stateChanged(
									new AnalogReadChangeEvent(entry.getKey(),
											average(entry.getValue()),
											"alp://todo/rebuild/message"));
				}
				data.clear();
			}
		}
	}


}