package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.Tone;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoTone;

public class DefaultToArduinoTone implements ToArduinoTone {

	private final Tone tone;

	public DefaultToArduinoTone(Tone tone) {
		this.tone = tone;
	}

	@Override
	public Tone getTone() {
		return tone;
	}

}
