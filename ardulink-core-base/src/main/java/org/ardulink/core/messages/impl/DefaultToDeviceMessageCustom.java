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

import static java.lang.String.join;
import static org.ardulink.util.anno.LapsedWith.JDK14;

import java.util.List;

import org.ardulink.core.messages.api.ToDeviceMessageCustom;
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
public class DefaultToDeviceMessageCustom implements ToDeviceMessageCustom {

	public static ToDeviceMessageCustom toDeviceMessageCustom(String... messages) {
		return toDeviceMessageCustom(List.of(messages));
	}

	public static ToDeviceMessageCustom toDeviceMessageCustom(List<String> messages) {
		return new DefaultToDeviceMessageCustom(messages);
	}

	private final List<String> messages;

	private DefaultToDeviceMessageCustom(List<String> messages) {
		this.messages = List.copyOf(messages);
	}

	@Override
	public List<String> getMessages() {
		return messages;
	}

	@Override
	public String toString() {
		return join(" ", messages);
	}

}
