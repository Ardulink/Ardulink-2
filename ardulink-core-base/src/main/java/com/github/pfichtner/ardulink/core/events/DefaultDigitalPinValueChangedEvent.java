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

package com.github.pfichtner.ardulink.core.events;

import com.github.pfichtner.ardulink.core.Pin.DigitalPin;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DefaultDigitalPinValueChangedEvent implements
		DigitalPinValueChangedEvent {

	private final DigitalPin pin;
	private final Boolean value;

	public DefaultDigitalPinValueChangedEvent(DigitalPin pin, boolean value) {
		this.pin = pin;
		this.value = value;
	}

	public DigitalPin getPin() {
		return this.pin;
	}

	public Boolean getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "DefaultDigitalPinValueChangedEvent [pin=" + pin + ", value="
				+ value + "]";
	}

}
