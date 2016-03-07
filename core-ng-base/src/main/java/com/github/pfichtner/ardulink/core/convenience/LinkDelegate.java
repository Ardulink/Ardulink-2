/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.github.pfichtner.ardulink.core.convenience;

import java.io.IOException;

import com.github.pfichtner.ardulink.core.ConnectionListener;
import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Tone;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.events.EventListener;
import com.github.pfichtner.ardulink.core.events.RplyListener;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
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

	@Override
	public Link addRplyListener(RplyListener listener) throws IOException {
		return getDelegate().addRplyListener(listener);
	}

	@Override
	public Link removeRplyListener(RplyListener listener) throws IOException {
		return getDelegate().removeRplyListener(listener);
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
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		getDelegate().sendNoTone(analogPin);
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		getDelegate().sendCustomMessage(messages);
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
