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
package org.ardulink.core.proto.lua;

import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.lua.LuaProtoBuilder.getBuilder;
import static org.ardulink.core.proto.lua.LuaProtoBuilder.LuaProtocolKey.CUSTOM_MESSAGE;
import static org.ardulink.core.proto.lua.LuaProtoBuilder.LuaProtocolKey.POWER_PIN_INTENSITY;
import static org.ardulink.core.proto.lua.LuaProtoBuilder.LuaProtocolKey.POWER_PIN_SWITCH;
import static org.ardulink.core.proto.lua.LuaProtoBuilder.LuaProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.lua.LuaProtoBuilder.LuaProtocolKey.STOP_LISTENING_DIGITAL;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessagePing;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.ardulink.ArdulinkProtocol2.ALPByteStreamProcessor;
import org.ardulink.util.Bytes;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * LUA protocol is intended to be used with NodeMCU LUA firmware. No upload
 * sketches/scripts are needed. However there are some limitations.
 * 
 * Start and Stop Analog PINs messages are not supported Key press message is
 * not supported Tone and NoTone messages are not supported
 * 
 * Incoming messages are computed as Ardulink protocol.
 * 
 * [adsense]
 *
 */
public class LuaProtocol implements Protocol {

	private final class LuaProtocolByteStreamProcessor extends ALPByteStreamProcessor {

		@Override
		public byte[] toDevice(ToDeviceMessagePing ping) {
			throw noSense();
		}

		@Override
		public byte[] toDevice(ToDeviceMessageStartListening startListening) {
			Pin pin = startListening.getPin();
			if (pin.is(DIGITAL)) {
				return toBytes(getBuilder(START_LISTENING_DIGITAL).forPin(pin.pinNum()).build());
			}
			if (pin.is(ANALOG)) {
				throw notSupported("Start Listening");
			}
			throw illegalPinType(pin);
		}

		@Override
		public byte[] toDevice(ToDeviceMessageStopListening stopListening) {
			Pin pin = stopListening.getPin();
			if (pin.is(DIGITAL)) {
				return toBytes(getBuilder(STOP_LISTENING_DIGITAL).forPin(pin.pinNum()).build());
			}
			if (pin.is(ANALOG)) {
				throw notSupported("Stop Listening");
			}
			throw illegalPinType(pin);
		}

		@Override
		public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
			if (pinStateChange.getPin().is(ANALOG)) {
				return toBytes(getBuilder(POWER_PIN_INTENSITY).forPin(pinStateChange.getPin().pinNum())
						.withValue(pinStateChange.getValue()).build());
			}
			if (pinStateChange.getPin().is(DIGITAL)) {
				return toBytes(getBuilder(POWER_PIN_SWITCH).forPin(pinStateChange.getPin().pinNum())
						.withValue(pinStateChange.getValue()).build());
			}
			throw illegalPinType(pinStateChange.getPin());
		}

		@Override
		public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
			throw noSense();
		}

		@Override
		public byte[] toDevice(ToDeviceMessageTone tone) {
			throw noSense();
		}

		@Override
		public byte[] toDevice(ToDeviceMessageNoTone noTone) {
			throw noSense();
		}

		@Override
		public byte[] toDevice(ToDeviceMessageCustom custom) {
			return toBytes(getBuilder(CUSTOM_MESSAGE).withValues((Object[]) custom.getMessages()).build());
		}

		/**
		 * Appends the separator to the passed message. This is not done using string
		 * concatenations but in a byte[] for performance reasons.
		 * 
		 * @param message the message to send
		 * @return byte[] holding the passed message and the protocol's divider
		 */
		@Override
		public byte[] toBytes(String message) {
			return Bytes.concat(message.getBytes(), SEPARATOR);
		}

		private UnsupportedOperationException notSupported(String type) {
			return new UnsupportedOperationException(type + " message not supported for " + getName() + " protocol");
		}

		private UnsupportedOperationException noSense() {
			return new UnsupportedOperationException("This message has no sense for " + getName() + " protocol");
		}

	}

	public static final String NAME = "LUA";
	private static final byte[] SEPARATOR = "\r\n".getBytes();

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public ByteStreamProcessor newByteStreamProcessor() {
		return new LuaProtocolByteStreamProcessor();
	}

}
