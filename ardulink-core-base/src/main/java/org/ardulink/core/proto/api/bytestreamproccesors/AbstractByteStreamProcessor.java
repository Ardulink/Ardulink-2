package org.ardulink.core.proto.api.bytestreamproccesors;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.util.ByteArray;

public abstract class AbstractByteStreamProcessor implements ByteStreamProcessor {

	private final List<RawListener> rawListeners = new CopyOnWriteArrayList<RawListener>();
	private final List<FromDeviceListener> listeners = new CopyOnWriteArrayList<FromDeviceListener>();

	protected final ByteArray byteArray = new ByteArray();

	@Override
	public void process(byte[] bytes) {
		for (byte b : bytes) {
			process(b);
		}
	}

	@Override
	public void addListener(RawListener listener) {
		rawListeners.add(listener);
	}

	@Override
	public void addListener(FromDeviceListener listener) {
		listeners.add(listener);
	}

	protected void fireEvent(FromDeviceMessage fromDevice) {
		byte[] bytesProcessed = byteArray.copy();
		byteArray.clear();
		for (RawListener listener : rawListeners) {
			listener.handle(bytesProcessed);
		}
		for (FromDeviceListener listener : listeners) {
			listener.handle(fromDevice);
		}
	}

}