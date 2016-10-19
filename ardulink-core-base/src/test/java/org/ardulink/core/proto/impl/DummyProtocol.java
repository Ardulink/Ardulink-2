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

package org.ardulink.core.proto.impl;

import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.proto.api.Protocol;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DummyProtocol implements Protocol {

	private static final DummyProtocol instance = new DummyProtocol();

	public static Protocol getInstance() {
		return instance;
	}

	@Override
	public String getName() {
		return "dummyProto";
	}

	@Override
	public byte[] getSeparator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStartListening startListening) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStopListening stopListening) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageTone tone) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageNoTone noTone) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageCustom custom) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FromDeviceMessage fromDevice(byte[] bytes) {
		throw new UnsupportedOperationException();
	}

}
