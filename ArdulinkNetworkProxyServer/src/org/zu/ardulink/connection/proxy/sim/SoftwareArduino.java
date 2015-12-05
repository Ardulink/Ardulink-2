package org.zu.ardulink.connection.proxy.sim;

import java.io.InputStream;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.Pin.Type;
import com.github.pfichtner.ardulink.core.StreamReader;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocol.FromArduino;

public abstract class SoftwareArduino extends StreamReader {

	private final Protocol protocol;

	public SoftwareArduino(Protocol protocol, InputStream inputStream) {
		super(inputStream);
		this.protocol = protocol;
		runReaderThread("\n");
	}

	@Override
	protected void received(byte[] bytes) throws Exception {
		FromArduino fromArduino = protocol.fromArduino(bytes);
		Pin pin = fromArduino.getPin();
		if (pin.is(Type.DIGITAL) && fromArduino.getValue() instanceof Boolean) {
			DigitalPin digitalPin = (DigitalPin) pin;
			switchDigitalPin(digitalPin, (Boolean) fromArduino.getValue());
		}
		if (pin.is(Type.ANALOG) && fromArduino.getValue() instanceof Integer) {
			switchAnalogPin((AnalogPin) pin, (Integer) fromArduino.getValue());
		}
	}

	protected abstract void switchDigitalPin(DigitalPin digitalPin,
			Boolean value);

	protected abstract void switchAnalogPin(AnalogPin analogPin, Integer value);

}
