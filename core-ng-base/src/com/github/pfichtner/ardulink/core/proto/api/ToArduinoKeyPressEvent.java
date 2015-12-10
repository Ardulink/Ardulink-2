package com.github.pfichtner.ardulink.core.proto.api;

public class ToArduinoKeyPressEvent {

	public final char keychar;
	public final int keycode;
	public final int keylocation;
	public final int keymodifiers;
	public final int keymodifiersex;

	public ToArduinoKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) {
		this.keychar = keychar;
		this.keycode = keycode;
		this.keylocation = keylocation;
		this.keymodifiers = keymodifiers;
		this.keymodifiersex = keymodifiersex;
	}

}
