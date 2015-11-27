package com.github.pfichtner.ardulink.core;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;

public final class Pins {

	private Pins() {
		super();
	}

	public static boolean isDigital(Pin pin) {
		return pin instanceof DigitalPin;
	}

	public static boolean isAnalog(Pin pin) {
		return pin instanceof AnalogPin;
	}

}
