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

package org.ardulink.core.messages.impl;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DefaultFromDeviceMessagePinStateChanged implements FromDeviceMessagePinStateChanged {

	private final Pin pin;
	private final Object value;

	public DefaultFromDeviceMessagePinStateChanged(Pin pin, Object value) {
		this.pin = pin;
		this.value = value;
	}

	public Pin getPin() {
		return this.pin;
	}

	public Object getValue() {
		return this.value;
	}

}
