package com.github.pfichtner.ardulink.core.proto.api;

import com.github.pfichtner.ardulink.core.Tone;

public class ToArduinoTone {

	public Long messageId;
	public final Tone tone;

	public ToArduinoTone(Tone tone) {
		this.tone = tone;
	}

	public ToArduinoTone(long messageId, Tone tone) {
		this(tone);
		this.messageId = messageId;
	}

}
