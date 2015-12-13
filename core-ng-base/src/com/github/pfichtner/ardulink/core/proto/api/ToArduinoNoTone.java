package com.github.pfichtner.ardulink.core.proto.api;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;

public class ToArduinoNoTone {

	public Long messageId;
	public final AnalogPin analogPin;

	public ToArduinoNoTone(AnalogPin analogPin) {
		this.analogPin = analogPin;
	}

	public ToArduinoNoTone(long messageId, AnalogPin analogPin) {
		this(analogPin);
		this.messageId = messageId;
	}

}
