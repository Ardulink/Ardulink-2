package org.ardulink.camel.test;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.events.CustomListener;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.RplyListener;

public class TestLink implements Link {

	private final TestLinkConfig config;

	public TestLink(TestLinkConfig config) {
		this.config = config;
	}
	
	public TestLinkConfig getConfig() {
		return config;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public Link addListener(EventListener listener) throws IOException {
		return null;
	}

	@Override
	public Link removeListener(EventListener listener) throws IOException {
		return null;
	}

	@Override
	public Link addRplyListener(RplyListener listener) throws IOException {
		return null;
	}

	@Override
	public Link removeRplyListener(RplyListener listener) throws IOException {
		return null;
	}

	@Override
	public Link addCustomListener(CustomListener listener) throws IOException {
		return null;
	}

	@Override
	public Link removeCustomListener(CustomListener listener)
			throws IOException {
		return null;
	}

	@Override
	public long startListening(Pin pin) throws IOException {
		return 0;
	}

	@Override
	public long stopListening(Pin pin) throws IOException {
		return 0;
	}

	@Override
	public long switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		return 0;
	}

	@Override
	public long switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		return 0;
	}

	@Override
	public long sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		return 0;
	}

	@Override
	public long sendTone(Tone tone) throws IOException {
		return 0;
	}

	@Override
	public long sendNoTone(AnalogPin analogPin) throws IOException {
		return 0;
	}

	@Override
	public long sendCustomMessage(String... messages) throws IOException {
		return 0;
	}

}
