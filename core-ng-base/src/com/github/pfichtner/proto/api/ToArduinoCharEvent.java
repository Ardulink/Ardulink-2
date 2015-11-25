package com.github.pfichtner.proto.api;

public class ToArduinoCharEvent {

	public final char keychar;
	public final int keycode;
	public final int keylocation;
	public final int keymodifiers;
	public final int keymodifiersex;

	public ToArduinoCharEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) {
		this.keychar = keychar;
		this.keycode = keycode;
		this.keylocation = keylocation;
		this.keymodifiers = keymodifiers;
		this.keymodifiersex = keymodifiersex;
	}

}
