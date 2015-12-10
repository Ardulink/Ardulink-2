package com.github.pfichtner.ardulink.core.convenience;

import java.io.IOException;

import com.github.pfichtner.ardulink.core.ConnectionListener;
import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Tone;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.events.EventListener;

public class LinkDelegate implements Link {

	private final Link delegate;

	public LinkDelegate(Link delegate) {
		this.delegate = delegate;
	}

	public Link getDelegate() {
		return delegate;
	}

	public Link addListener(EventListener listener) throws IOException {
		return getDelegate().addListener(listener);
	}

	public Link removeListener(EventListener listener) throws IOException {
		return getDelegate().removeListener(listener);
	}

	public void startListening(Pin pin) throws IOException {
		getDelegate().startListening(pin);
	}

	public void close() throws IOException {
		getDelegate().close();
	}

	public void stopListening(Pin pin) throws IOException {
		getDelegate().stopListening(pin);
	}

	public void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		getDelegate().switchAnalogPin(analogPin, value);
	}

	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		getDelegate().switchDigitalPin(digitalPin, value);
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		getDelegate().sendTone(tone);
	}

	@Override
	public void sendNoTone() throws IOException {
		getDelegate().sendNoTone();
	}

	@Override
	public void sendCustomMessage(String message) throws IOException {
		getDelegate().sendCustomMessage(message);
	}

	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		getDelegate().sendKeyPressEvent(keychar, keycode, keylocation,
				keymodifiers, keymodifiersex);
	}

	public Link addConnectionListener(ConnectionListener connectionListener) {
		return getDelegate().addConnectionListener(connectionListener);
	}

	public Link removeConnectionListener(ConnectionListener connectionListener) {
		return getDelegate().removeConnectionListener(connectionListener);
	}

}
