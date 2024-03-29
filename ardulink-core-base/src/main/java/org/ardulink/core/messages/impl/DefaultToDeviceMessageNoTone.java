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

import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DefaultToDeviceMessageNoTone implements ToDeviceMessageNoTone {

	public static ToDeviceMessageNoTone toDeviceMessageNoTone(AnalogPin analogPin) {
		return new DefaultToDeviceMessageNoTone(analogPin);
	}

	private final AnalogPin analogPin;

	public DefaultToDeviceMessageNoTone(AnalogPin analogPin) {
		this.analogPin = analogPin;
	}

	@Override
	public AnalogPin getAnalogPin() {
		return analogPin;
	}

}
