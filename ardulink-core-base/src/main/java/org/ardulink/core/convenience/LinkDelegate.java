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

package org.ardulink.core.convenience;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.events.CustomListener;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.RplyListener;

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

	@Override
	public Link addListener(EventListener listener) throws IOException {
		return getDelegate().addListener(listener);
	}

	@Override
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

	@Override
	public long startListening(Pin pin) throws IOException {
		return getDelegate().startListening(pin);
	}

	@Override
	public void close() throws IOException {
		getDelegate().close();
	}

	@Override
	public long stopListening(Pin pin) throws IOException {
		return getDelegate().stopListening(pin);
	}

	@Override
	public long switchAnalogPin(AnalogPin analogPin, int value) throws IOException {
		return getDelegate().switchAnalogPin(analogPin, value);
	}

	@Override
	public long switchDigitalPin(DigitalPin digitalPin, boolean value) throws IOException {
		return getDelegate().switchDigitalPin(digitalPin, value);
	}

	@Override
	public long sendTone(Tone tone) throws IOException {
		return getDelegate().sendTone(tone);
	}

	@Override
	public long sendNoTone(AnalogPin analogPin) throws IOException {
		return getDelegate().sendNoTone(analogPin);
	}

	@Override
	public long sendCustomMessage(String... messages) throws IOException {
		return getDelegate().sendCustomMessage(messages);
	}

	@Override
	public long unlock(String secret) throws IOException {
		return getDelegate().unlock(secret);
	}

	@Override
	public long sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex)
			throws IOException {
		return getDelegate().sendKeyPressEvent(keychar, keycode, keylocation, keymodifiers, keymodifiersex);
	}

	@Override
	public Link addCustomListener(CustomListener listener) throws IOException {
		return getDelegate().addCustomListener(listener);
	}

	@Override
	public Link removeCustomListener(CustomListener listener) throws IOException {
		return getDelegate().removeCustomListener(listener);
	}

}
