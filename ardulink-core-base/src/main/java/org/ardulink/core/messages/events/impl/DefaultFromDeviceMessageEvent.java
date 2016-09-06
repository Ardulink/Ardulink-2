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
package org.ardulink.core.messages.events.impl;

import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.events.api.FromDeviceMessageEvent;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DefaultFromDeviceMessageEvent implements FromDeviceMessageEvent {

	private FromDeviceMessage fromDeviceMessage;
	
	public DefaultFromDeviceMessageEvent(FromDeviceMessage fromDeviceMessage) {
		this.fromDeviceMessage = fromDeviceMessage;
	}

	@Override
	public FromDeviceMessage getFromDeviceMessage() {
		return fromDeviceMessage;
	}

}
