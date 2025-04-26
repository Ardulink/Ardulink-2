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

package org.ardulink.core.events;

import static org.ardulink.util.anno.LapsedWith.JDK14;

import java.util.Objects;

import org.ardulink.core.Pin;
import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@LapsedWith(value = JDK14, module = "records")
public class FilteredEventListenerAdapter extends EventListenerAdapter {

	private final Pin pin;
	private final EventListener delegate;

	public FilteredEventListenerAdapter(Pin pin, EventListener delegate) {
		this.pin = pin;
		this.delegate = delegate;
	}

	public Pin getPin() {
		return pin;
	}

	@Override
	public void stateChanged(AnalogPinValueChangedEvent event) {
		if (accept(event)) {
			delegate.stateChanged(event);
		}
	}

	@Override
	public void stateChanged(DigitalPinValueChangedEvent event) {
		if (accept(event)) {
			delegate.stateChanged(event);
		}
	}

	private boolean accept(PinValueChangedEvent event) {
		return Objects.equals(pin, event.getPin());
	}

}
