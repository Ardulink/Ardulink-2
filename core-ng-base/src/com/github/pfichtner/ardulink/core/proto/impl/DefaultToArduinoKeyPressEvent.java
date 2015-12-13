package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.proto.api.ToArduinoKeyPressEvent;

public class DefaultToArduinoKeyPressEvent implements ToArduinoKeyPressEvent {

	private final char keychar;
	private final int keycode;
	private final int keylocation;
	private final int keymodifiers;
	private final int keymodifiersex;

	public DefaultToArduinoKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) {
		this.keychar = keychar;
		this.keycode = keycode;
		this.keylocation = keylocation;
		this.keymodifiers = keymodifiers;
		this.keymodifiersex = keymodifiersex;
	}

	@Override
	public char getKeychar() {
		return keychar;
	}

	@Override
	public int getKeycode() {
		return keycode;
	}

	@Override
	public int getKeylocation() {
		return keylocation;
	}

	@Override
	public int getKeymodifiers() {
		return keymodifiers;
	}

	@Override
	public int getKeymodifiersex() {
		return keymodifiersex;
	}

}
