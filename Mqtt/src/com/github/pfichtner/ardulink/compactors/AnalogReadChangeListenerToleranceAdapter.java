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

import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class AnalogReadChangeListenerToleranceAdapter extends
		AnalogReadChangeListenerAdapter {

	private Integer cachedValue;
	private final Tolerance tolerance;

	public AnalogReadChangeListenerToleranceAdapter(Tolerance tolerance,
			AnalogReadChangeListener delegate) {
		super(delegate);
		this.tolerance = tolerance;
	}

	@Override
	public void stateChanged(AnalogReadChangeEvent e) {
		int newValue = e.getValue();
		if (this.cachedValue == null
				|| !tolerance
						.inTolerance(this.cachedValue.intValue(), newValue)
				|| isHighOrLowValue(newValue)) {
			this.cachedValue = Integer.valueOf(newValue);
			getDelegate().stateChanged(e);
		}
	}

	private boolean isHighOrLowValue(int value) {
		return value == 0 || value == 255;
	}

}