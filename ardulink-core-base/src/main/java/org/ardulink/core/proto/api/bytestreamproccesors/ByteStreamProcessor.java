package org.ardulink.core.proto.api.bytestreamproccesors;

import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;

public interface ByteStreamProcessor {

	public static interface FromDeviceListener {
		void handle(FromDeviceMessage fromDevice);
	}

	public static interface RawListener {
		void handle(byte[] fromDevice);
	}

	void addListener(FromDeviceListener listener);

	void addListener(RawListener listener);

	// -- in

	void process(byte[] read);

	void process(byte read);
	
	// -- out

	byte[] toDevice(ToDeviceMessageStartListening startListening);

	byte[] toDevice(ToDeviceMessageStopListening stopListening);

	byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange);

	byte[] toDevice(ToDeviceMessageKeyPress keyPress);

	byte[] toDevice(ToDeviceMessageTone tone);

	byte[] toDevice(ToDeviceMessageNoTone noTone);

	byte[] toDevice(ToDeviceMessageCustom custom);

}