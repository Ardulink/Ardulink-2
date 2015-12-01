package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoCharEvent;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoPinEvent;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStartListening;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStopListening;

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
	public byte[] toArduino(ToArduinoCharEvent charEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FromArduino fromArduino(byte[] bytes) {
		throw new UnsupportedOperationException();
	}

}
