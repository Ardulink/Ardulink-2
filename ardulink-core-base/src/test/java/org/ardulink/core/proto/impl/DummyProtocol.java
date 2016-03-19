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

import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.ToArduinoCustomMessage;
import org.ardulink.core.proto.api.ToArduinoKeyPressEvent;
import org.ardulink.core.proto.api.ToArduinoNoTone;
import org.ardulink.core.proto.api.ToArduinoPinEvent;
import org.ardulink.core.proto.api.ToArduinoStartListening;
import org.ardulink.core.proto.api.ToArduinoStopListening;
import org.ardulink.core.proto.api.ToArduinoTone;

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
	public byte[] toArduino(ToArduinoStartListening startListeningEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoStopListening stopListeningEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoPinEvent pinEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoKeyPressEvent charEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoTone tone) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoNoTone noTone) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduino(ToArduinoCustomMessage customMessage) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FromArduino fromArduino(byte[] bytes) {
		throw new UnsupportedOperationException();
	}

}
