package org.ardulink.core.proto.api.bytestreamproccesors;

import org.ardulink.core.messages.api.FromDeviceMessage;

public interface ByteStreamProcessor {

	public static interface FromDeviceListener {
		void handle(FromDeviceMessage fromDevice);
	}

	void addListener(ByteStreamProcessor.FromDeviceListener listener);

	void process(byte[] read);

}