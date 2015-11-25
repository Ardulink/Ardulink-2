package com.github.pfichtner;

import com.github.pfichtner.Pin.AnalogPin;
import com.github.pfichtner.Pin.DigitalPin;

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
