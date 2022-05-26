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

package org.ardulink.core.proto.api;

import static org.ardulink.util.anno.LapsedWith.NEXT_ARDULINK_VERSION_REFACTORING_DONE;

import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.util.anno.LapsedWith;


/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public interface Protocol {

	String getName();

	@Deprecated
	@LapsedWith(NEXT_ARDULINK_VERSION_REFACTORING_DONE)
	byte[] getSeparator();

	byte[] toDevice(ToDeviceMessageStartListening startListening);

	byte[] toDevice(ToDeviceMessageStopListening stopListening);

	byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange);

	byte[] toDevice(ToDeviceMessageKeyPress keyPress);

	byte[] toDevice(ToDeviceMessageTone tone);

	byte[] toDevice(ToDeviceMessageNoTone noTone);

	byte[] toDevice(ToDeviceMessageCustom custom);

	FromDeviceMessage fromDevice(byte[] bytes);

}
