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

import java.util.Objects;

import org.ardulink.core.Pin.DigitalPin;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class DefaultDigitalPinValueChangedEvent implements DigitalPinValueChangedEvent {

	public static DefaultDigitalPinValueChangedEvent digitalPinValueChanged(DigitalPin pin, boolean value) {
		return new DefaultDigitalPinValueChangedEvent(pin, value);
	}

	private final DigitalPin pin;
	private final Boolean value;

	public DefaultDigitalPinValueChangedEvent(DigitalPin pin, boolean value) {
		this.pin = pin;
		this.value = value;
	}

	@Override
	public DigitalPin getPin() {
		return this.pin;
	}

	@Override
	public Boolean getValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pin, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultDigitalPinValueChangedEvent other = (DefaultDigitalPinValueChangedEvent) obj;
		return Objects.equals(pin, other.pin) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "DefaultDigitalPinValueChangedEvent [pin=" + pin + ", value=" + value + "]";
	}

}
