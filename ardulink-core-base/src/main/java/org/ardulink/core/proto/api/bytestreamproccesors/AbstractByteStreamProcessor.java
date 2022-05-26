package org.ardulink.core.proto.api.bytestreamproccesors;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.messages.api.FromDeviceMessage;

public abstract class AbstractByteStreamProcessor implements ByteStreamProcessor {

	private final List<FromDeviceListener> listeners = new CopyOnWriteArrayList<FromDeviceListener>();

	@Override
	public void process(byte[] bytes) {
		for (byte b : bytes) {
			process(b);
		}
	}

	@Override
	public void addListener(FromDeviceListener listener) {
		listeners.add(listener);
	}

	protected void fireEvent(FromDeviceMessage fromDevice) {
		for (FromDeviceListener listener : listeners) {
			listener.handle(fromDevice);
		}
	}

}