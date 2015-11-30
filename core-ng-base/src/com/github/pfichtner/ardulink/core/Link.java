package com.github.pfichtner.ardulink.core;

import java.io.Closeable;
import java.io.IOException;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.events.EventListener;

public interface Link extends Closeable {

	Link addListener(EventListener listener) throws IOException;

	Link removeListener(EventListener listener) throws IOException;

	void startListening(Pin pin) throws IOException;

	void stopListening(Pin pin) throws IOException;

	void switchAnalogPin(AnalogPin analogPin, Integer value) throws IOException;

	void switchDigitalPin(DigitalPin digitalPin, Boolean value)
			throws IOException;

	void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException;

}