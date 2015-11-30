package com.github.pfichtner.core.mqtt;

import java.io.IOException;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.events.EventListener;

public class MqttLink implements Link {

	private final MqttLinkConfig config;

	public MqttLink(MqttLinkConfig config) {
		this.config = config;
	}

	@Override
	public Link addListener(EventListener listener) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Link removeListener(EventListener listener) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void startListening(Pin pin) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, Integer value)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, Boolean value)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		throw new UnsupportedOperationException();
	}
}
