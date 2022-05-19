package org.ardulink.core.proto.api.bytestreamproccesors;

import org.ardulink.util.ByteArray;

public abstract class AbstractState implements State {

	private final ByteArray byteArray = new ByteArray(64);

	protected void bufferAppend(byte b) {
		byteArray.append(b);
	}

	protected int bufferLength() {
		return byteArray.length();
	}

	protected byte[] copyOfBuffer() {
		return byteArray.copy();
	}

}