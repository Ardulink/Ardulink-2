package org.ardulink.core.proto.api;

import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;

public interface ProtocolNG {

	String getName();

	byte[] toDevice(ToDeviceMessageStartListening startListening);

	byte[] toDevice(ToDeviceMessageStopListening stopListening);

	byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange);

	byte[] toDevice(ToDeviceMessageKeyPress keyPress);

	byte[] toDevice(ToDeviceMessageTone tone);

	byte[] toDevice(ToDeviceMessageNoTone noTone);

	byte[] toDevice(ToDeviceMessageCustom custom);

	ByteStreamProcessor newByteStreamProcessor();
	
}