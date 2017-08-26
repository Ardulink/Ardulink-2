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
import org.ardulink.core.messages.api.FromDeviceChangeListeningState;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DefaultFromDeviceChangeListeningState implements
		FromDeviceChangeListeningState {

	private final Pin pin;
	private final Mode mode;

	public DefaultFromDeviceChangeListeningState(Pin pin, Mode mode) {
		this.pin = pin;
		this.mode = mode;
	}

	public Pin getPin() {
		return this.pin;
	}

	public Mode getMode() {
		return mode;
	}

}
