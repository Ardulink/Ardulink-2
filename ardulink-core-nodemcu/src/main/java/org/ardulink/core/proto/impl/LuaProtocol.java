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

import static java.lang.System.arraycopy;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.impl.LuaProtoBuilder.getBuilder;
import static org.ardulink.core.proto.impl.LuaProtoBuilder.LuaProtocolKey.*;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessageCustom;
import org.ardulink.core.proto.api.Protocol;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * LUA protocol is intended to be used with NodeMCU LUA firmware. No upload sketches/scripts are needed.
 * However there are some limitations.
 * 
 * Start and Stop Analog PINs messages are not supported
 * Key press message is not supported
 * Tone and NoTone messages are not supported
 * 
 * Incoming messages starting with alp:// are computed as Ardulink protocol
 * other incoming messages are all custom messages.
 * 
 * [adsense]
 *
 */
public class LuaProtocol implements Protocol {

	private final String name = "LUA";
	private final byte[] separator = "\r\n".getBytes();

	private static final LuaProtocol instance = new LuaProtocol();

	public static Protocol instance() {
		return instance;
	}

	@Override
	public String getName() {
		return name;
	};

	@Override
	public byte[] getSeparator() {
		return separator;
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStartListening startListening) {
		Pin pin = startListening.getPin();
		if (pin.is(DIGITAL)) {
			return toBytes(
					 getBuilder(START_LISTENING_DIGITAL)
					    .forPin(pin.pinNum())
					   .build());
		}
		if(pin.is(ANALOG)) {
			throw new UnsupportedOperationException(String.format("Start Listening message not supported for %s protocol", getName()));
		}
		throw illegalPinType(pin);
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStopListening stopListening) {
		Pin pin = stopListening.getPin();
		if (pin.is(DIGITAL)) {
			return toBytes(
					 getBuilder(STOP_LISTENING_DIGITAL)
					    .forPin(pin.pinNum())
					   .build());
		}
		if(pin.is(ANALOG)) {
			throw new UnsupportedOperationException(String.format("Stop Listening message not supported for %s protocol", getName()));
		}
		throw illegalPinType(pin);
	}

	@Override
	public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
		if (pinStateChange.getPin().is(ANALOG)) {
			return toBytes(
					 getBuilder(POWER_PIN_INTENSITY)
					    .forPin(pinStateChange.getPin().pinNum())
					    .withValue((Integer) pinStateChange.getValue())
					   .build());
		}
		if (pinStateChange.getPin().is(DIGITAL)) {
			return toBytes(
					 getBuilder(POWER_PIN_SWITCH)
					    .forPin(pinStateChange.getPin().pinNum())
					    .withValue((Boolean) pinStateChange.getValue())
					   .build());
		}
		throw illegalPinType(pinStateChange.getPin());
	}

	@Override
	public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
		throw new UnsupportedOperationException(String.format("This message has no sense for %s protocol", getName()));
	}

	@Override
	public byte[] toDevice(ToDeviceMessageTone tone) {
		throw new UnsupportedOperationException(String.format("This message has no sense for %s protocol", getName()));
	}

	@Override
	public byte[] toDevice(ToDeviceMessageNoTone noTone) {
		throw new UnsupportedOperationException(String.format("This message has no sense for %s protocol", getName()));
	}

	@Override
	public byte[] toDevice(ToDeviceMessageCustom custom) {
		return toBytes(
				 getBuilder(CUSTOM_MESSAGE)
				    .withValues((Object[])custom.getMessages())
				   .build());	
	}

	@Override
	public FromDeviceMessage fromDevice(byte[] bytes) {
		
		String in = new String(bytes);
		if(in.startsWith("alp://")) {
			return ArdulinkProtocol2.instance().fromDevice(bytes);
		} else {
			return new DefaultFromDeviceMessageCustom(in);
		}
	}

	private IllegalStateException illegalPinType(Pin pin) {
		return new IllegalStateException("Illegal type " + pin.getType()
				+ " of pin " + pin);
	}
	
	/**
	 * Appends the separator to the passed message. This is not done using
	 * string concatenations but in a byte[] for performance reasons.
	 * 
	 * @param message
	 *            the message to send
	 * @return byte[] holding the passed message and the protocol's divider
	 */
	private byte[] toBytes(String message) {
		byte[] bytes = new byte[message.length() + separator.length];
		byte[] msgBytes = message.getBytes();
		arraycopy(msgBytes, 0, bytes, 0, msgBytes.length);
		arraycopy(separator, 0, bytes, msgBytes.length, separator.length);
		return bytes;
	}
}
